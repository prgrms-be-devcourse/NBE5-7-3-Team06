package programmers.team6.domain.vacation.service

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto
import programmers.team6.domain.vacation.dto.response.*
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository
import programmers.team6.domain.vacation.support.MonthRange
import programmers.team6.domain.vacation.util.mapper.VacationMapper
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.NotFoundException
import java.time.YearMonth

@Service
@Transactional
class VacationService(
    private val vacationInfoRepository: VacationInfoRepository,
    private val vacationMapper: VacationMapper,
    private val vacationRequestRepository: VacationRequestRepository,
    private val approvalStepRepository: ApprovalStepRepository,
    private val memberRepository: MemberRepository,
    private val codeRepository: CodeRepository,
    private val approvalStepService: ApprovalStepService,
    private val vacationRequestSearchRepository: VacationRequestSearchRepository
) {

    // 본인 연차 조회
    @Transactional(readOnly = true)
    fun getMyVacationInfo(memberId: Long): VacationInfoSelectResponseDto {
        getMemberById(memberId)

        val vacationInfo = vacationInfoRepository.findByMemberIdAndVacationType(
            memberId,
            VacationCode.ANNUAL.code
        ) ?: throw RuntimeException("휴가 정보를 찾을 수 없습니다.")

        return vacationMapper.toVacationInfoSelectResponseDto(vacationInfo)
    }

    // 휴가 신청
    fun requestVacation(memberId: Long, requestDto: VacationCreateRequestDto): VacationCreateResponseDto {
        // 신청자 정보 조회
        val member = memberRepository.findByIdWithDeptAndLeader(memberId)
            .orElse(null) ?: throw RuntimeException("멤버 정보를 찾을 수 없습니다.")

        // 시작일(from)과 종료일(to) 설정 (진행중이거나 승인된 휴가 기간내에 신청 불가능하게)
        if (vacationRequestRepository.countInRangeFromBetweenToBy(
                memberId,
                requestDto.from,
                requestDto.to
            ) != 0L
        ) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_VACATION_OVERLAP)
        }

        // 신청하려는 휴가 일수 계산
        val requestDays = vacationRequestRepository.calculateRequestedVacationDays(
            requestDto.from,
            requestDto.to,
            requestDto.vacationType
        )

        val actualRemainCount = vacationInfoRepository
            .findActualRemainingVacationDays(memberId, getVacationInfoType(requestDto.vacationType))
            .orElse(null) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_INFO)

        // 잔여 일수 초과 검증
        if (actualRemainCount < requestDays) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_INSUFFICIENT_VACATION_DAYS)
        }

        // 부서장 조회 (결재자)
        val dept = member.dept
        val approver = dept!!.deptLeader

        // 휴가 유형 코드 조회
        val vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", requestDto.vacationType)
            ?: throw RuntimeException("잘못된 휴가 유형입니다.")

        // 휴가 요청 상태 코드 (기본 대기 상태)
        val status = VacationRequestStatus.IN_PROGRESS

        // 휴가 요청 생성
        val vacationRequest = vacationMapper.toVacationRequest(requestDto, vacationType, status, member)

        // 저장
        vacationRequestRepository.save(vacationRequest)

        // 결재 단계 생성
        approvalStepService.saveApprovalStep(approver!!, vacationRequest)

        // 응답 DTO 생성
        return vacationMapper.toVacationCreateResponseDto(
            vacationRequest,
            vacationType.name,
            status,
            approver.name
        )
    }

    // 휴가 신청 내역 (페이징: 20개씩)
    @Transactional(readOnly = true)
    fun getVacationRequestList(memberId: Long, page: Int): VacationListResponseDto {
        // 사용자 존재 여부 확인
        getMemberById(memberId)

        // 페이지 요청 객체 생성 (페이지 번호는 0부터 시작)
        val pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        // 1. ID만 페이징해서 가져오기
        val idPage = vacationRequestRepository.findIdsByRequesterIdPaging(memberId, pageable)

        if (idPage.isEmpty) {
            // 빈 결과 반환
            return VacationListResponseDto(
                content = emptyList(),
                pageNumber = page,
                pageSize = pageable.pageSize,
                totalElements = idPage.totalElements,
                totalPages = idPage.totalPages,
                first = idPage.isFirst,
                last = idPage.isLast
            )
        }

        // 2. 페이징된 ID로 상세 정보 조회 (페치 조인 사용)
        val vacationRequests = vacationRequestRepository.findByIdsWithFetch(idPage.content.toMutableList())

        // 3. 결재 단계 정보 일괄 조회
        val requestIds: List<Long> = vacationRequests.mapNotNull { it.id }

        val approvalStepMap: Map<Long, ApprovalStep> =
            approvalStepRepository.findFirstStepsByVacationRequestIds(requestIds)
                .associateBy { it.vacationRequest.id!! }

        // 4. DTO 변환
        val content = vacationRequests.map { request ->
            val approvalStep = approvalStepMap[request.id]
            val approverName = approvalStep?.member?.name ?: "미지정"

            vacationMapper.toVacationCreateResponseDto(
                request,
                request.type.name,
                request.status,
                approverName
            )
        }

        // 5. 페이징 응답 DTO 생성
        return vacationMapper.toVacationListResponseDto(
            PageImpl(vacationRequests, pageable, idPage.totalElements),
            content
        )
    }

    // 휴가 신청 수정
    fun updateVacationRequest(
        memberId: Long,
        requestId: Long,
        requestDto: VacationUpdateRequestDto
    ): VacationUpdateResponseDto {
        // 요청자 확인
        getMemberById(memberId)

        // 휴가 신청 조회
        val vacationRequest = vacationRequestRepository.findById(requestId).orElse(null)
            ?: throw RuntimeException("휴가 신청 정보를 찾을 수 없습니다.")

        // 시작일(from)과 종료일(to) 설정 (진행중이거나 승인된 휴가 기간내에 신청 불가능하게)
        if (vacationRequestRepository.countInRangeFromBetweenToByExcludeRequestId(
                memberId, requestDto.from, requestDto.to, requestId
            ) != 0L
        ) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_VACATION_OVERLAP)
        }

        // 신청하려는 휴가 일수 계산
        val requestDays = vacationRequestRepository.calculateRequestedVacationDays(
            requestDto.from,
            requestDto.to,
            requestDto.vacationType
        )

        // 실제 사용 가능한 잔여 휴가 일수를 한 번에 조회
        val actualRemainCount = vacationInfoRepository
            .findActualRemainingVacationDaysExcludeRequestId(
                memberId,
                getVacationInfoType(requestDto.vacationType),
                requestId
            )!!.orElse(null) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_INFO)

        // 잔여 일수 초과 검증
        if (actualRemainCount < requestDays) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_INSUFFICIENT_VACATION_DAYS)
        }

        // 휴가 유형 코드 조회
        val vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", requestDto.vacationType)
            ?: throw RuntimeException("잘못된 휴가 유형입니다.")

        // 수정 권한 검증 및 수정 처리
        vacationRequest.updateByMember(memberId, requestDto.from, requestDto.to, requestDto.reason, vacationType)

        // 결재자 정보 조회
        val approvalStep = approvalStepRepository.findFirstByVacationRequestOrderByStepAsc(vacationRequest)
            ?: throw RuntimeException("결재 단계 정보를 찾을 수 없습니다.")

        // 응답 DTO 생성
        return vacationMapper.toVacationUpdateResponseDto(
            vacationRequest,
            vacationType.name,
            approvalStep.member.name
        )
    }

    // 대기중인 휴가 신청 취소
    fun cancelVacationRequest(memberId: Long, requestId: Long): Boolean {
        // 멤버 존재 여부 먼저 확인
        getMemberById(memberId)

        // 휴가 신청 조회
        val vacationRequest = vacationRequestRepository.findById(requestId).orElse(null)
            ?: throw RuntimeException("휴가 신청 정보를 찾을 수 없습니다.")

        // 휴가 신청 취소
        vacationRequest.validateAndCancel(memberId)

        // 1,2차 결재 취소
        approvalStepService.cancelApprovalStep(vacationRequest)

        return true
    }

    @Transactional(readOnly = true)
    fun selectVacationCalendar(yearMonthStr: String, deptId: Long?): List<VacationRequestCalendarResponse> {
        val monthRange = getMonthRange(yearMonthStr)

        return vacationRequestSearchRepository.findApprovedVacationsByMonth(
            VacationRequestStatus.APPROVED,
            monthRange.start,
            monthRange.end,
            deptId
        )
    }

    private fun getMonthRange(yearMonthStr: String): MonthRange {
        val yearMonth = YearMonth.parse(yearMonthStr)

        val start = yearMonth.atDay(1).atStartOfDay()
        val end = yearMonth.plusMonths(1).atDay(1).atStartOfDay()

        return MonthRange(start, end)
    }

    // 멤버 ID로 멤버를 조회, 멤버가 존재하지 않으면 예외 발생
    private fun getMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId).orElse(null)
            ?: throw RuntimeException("멤버 정보를 찾을 수 없습니다.")
    }

    // 반차 코드(05)를 01로 변환
    private fun getVacationInfoType(type: String): String {
        return if (type == "05") "01" else type
    }
}