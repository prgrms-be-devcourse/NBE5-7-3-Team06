package programmers.team6.domain.vacation.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.service.DeptService
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.request.ApprovalStepRejectRequest
import programmers.team6.domain.vacation.dto.request.ApprovalStepSelectRequest
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepDetailResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.domain.vacation.util.mapper.ApprovalStepMapper
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

@Service
class ApprovalStepService(
    private val approvalStepRepository: ApprovalStepRepository,
    private val vacationInfoRepository: VacationInfoRepository,
    private val vacationInfoLogPublisher: VacationInfoLogPublisher,
    private val deptService: DeptService
) {

    @Transactional(readOnly = true)
    fun findFirstStepByMemberId(memberId: Long, pageable: Pageable): Page<ApprovalFirstStepSelectResponse> {
        return approvalStepRepository.findFirstStepByMemberId(memberId, STEP1, pageable)
    }

    @Transactional(readOnly = true)
    fun findFirstStepByFilter(
        request: ApprovalStepSelectRequest, memberId: Long, pageable: Pageable
    ): Page<ApprovalFirstStepSelectResponse> {
        return approvalStepRepository.findFirstStepByFilter(
            memberId = memberId,
            type = request.type,
            name = request.name,
            from = request.from,
            to = request.to,
            status = request.status,
            step = STEP1,
            pageable = pageable
        )
    }

    @Transactional(readOnly = true)
    fun findSecondStepByMemberId(memberId: Long, pageable: Pageable): Page<ApprovalSecondStepSelectResponse> {
        return approvalStepRepository.findSecondStepByMemberId(memberId, STEP2, pageable)
    }

    @Transactional(readOnly = true)
    fun findSecondStepByFilter(
        request: ApprovalStepSelectRequest, memberId: Long, pageable: Pageable
    ): Page<ApprovalSecondStepSelectResponse> {
        return approvalStepRepository.findSecondStepByFilter(
            memberId = memberId,
            type = request.type,
            name = request.name,
            from = request.from,
            to = request.to,
            status = request.status,
            step = STEP2,
            pageable = pageable
        )
    }

    @Transactional(readOnly = true)
    fun findFirstStepDetailById(approvalStepId: Long, memberId: Long): ApprovalFirstStepDetailResponse {
        val findApprovalStep = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP1)
        return ApprovalStepMapper.fromFirstStepEntity(findApprovalStep)
    }

    @Transactional(readOnly = true)
    fun findSecondStepDetailById(approvalStepId: Long, memberId: Long): ApprovalSecondStepDetailResponse {
        val findApprovalStep = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP2)
        return ApprovalStepMapper.fromSecondStepEntity(findApprovalStep)
    }

    @Transactional
    fun approveFirstStep(approvalStepId: Long, memberId: Long) {
        val firstStepApproval = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP1)
        firstStepApproval.validateApprovable()

        val secondStepApproval = findByVacationRequestAndStep(firstStepApproval.getVacationRequest(), STEP2)

        firstStepApproval.approve()
        secondStepApproval.pending()

        if (firstStepApproval.isHrApprover) {
            approveSecondStep(secondStepApproval.getId(), memberId)
        }
    }

    @Transactional
    fun rejectFirstStep(approvalStepId: Long, memberId: Long, request: ApprovalStepRejectRequest) {
        val firstStepApproval = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP1)
        firstStepApproval.validateRejectable()

        val secondStepApproval = findByVacationRequestAndStep(firstStepApproval.getVacationRequest(), STEP2)

        firstStepApproval.reject(request.reason)
        secondStepApproval.reject(FIRST_STEP_REJECT_MESSAGE)
        firstStepApproval.rejectVacation()
    }

    @Transactional
    fun approveSecondStep(approvalStepId: Long, memberId: Long): Boolean {
        val findApprovalStep = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP2)
        findApprovalStep.validateApprovable()

        // todo: VacationInfoRepository 코틀린 변환 후 아래 주석으로 변경해야 함
        val findVacationInfo = vacationInfoRepository.findByMemberIdAndVacationType(
            findApprovalStep.vacationMemberId,
            if (findApprovalStep.isHalfDay) "HALF_DAY_CODE" else findApprovalStep.vacationCode
        ).orElseThrow {
            NotFoundException(
                NotFoundErrorCode.NOT_FOUND_VACATION_INFO
            )
        }
//        val findVacationInfo = vacationInfoRepository.findByMemberIdAndVacationType(
//            findApprovalStep.vacationMemberId,
//            if (findApprovalStep.isHalfDay) HALF_DAY_CODE else findApprovalStep.vacationCode
//        ) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_INFO)

        val count = if (findApprovalStep.isHalfDay) HALF_DAY_COUNT else findApprovalStep.calcVacationDays().toDouble()

        return if (findVacationInfo.canUseVacation(count)) {
            findApprovalStep.approve()
            findApprovalStep.approveVacation()
            val log = findVacationInfo.useVacation(count)
            vacationInfoLogPublisher.publish(log)
            true
        } else {
            findApprovalStep.cancel()
            findApprovalStep.cancelVacation()
            false
        }
    }

    @Transactional
    fun rejectSecondStep(approvalStepId: Long, memberId: Long, request: ApprovalStepRejectRequest) {
        val findApprovalStep = findByIdAndMemberIdAndStep(approvalStepId, memberId, STEP2)

        findApprovalStep.validateRejectable()
        findApprovalStep.reject(request.reason)
        findApprovalStep.rejectVacation()
    }

    // 휴가 신청 시 호출되어, 해당 멤버의 결재 단계 생성
    fun saveApprovalStep(firstApprover: Member, vacationRequest: VacationRequest) {
        // todo: 2차 결재자 지정 기능 (시스템상 구현 필요)
        val findDept = deptService.findByDeptName(HR_DEPT_NAME)
        approvalStepRepository.save(ApprovalStepMapper.toEntity(firstApprover, vacationRequest, STEP1))
        approvalStepRepository.save(ApprovalStepMapper.toEntity(findDept.deptLeader, vacationRequest, STEP2))
    }

    // 휴가 요청 취소될 경우, 관련 결재 단계 상태 CANCELED
    fun cancelApprovalStep(vacationStep: VacationRequest) {
        val findApprovalSteps = approvalStepRepository.findByVacationRequest(vacationStep)
        findApprovalSteps.forEach { it.cancel() }
    }

    private fun findByIdAndMemberIdAndStep(approvalStepId: Long, memberId: Long, step: Int): ApprovalStep {
        return approvalStepRepository.findByIdAndMemberIdAndStep(approvalStepId, memberId, step)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP)
    }

    private fun findByVacationRequestAndStep(vacationRequest: VacationRequest, step: Int): ApprovalStep {
        return approvalStepRepository.findByVacationRequestAndStep(vacationRequest, step)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP)
    }

    companion object {
        private const val STEP1 = 1
        private const val STEP2 = 2
        private const val HALF_DAY_CODE = "01"
        private const val HALF_DAY_COUNT = 0.5
        private const val HR_DEPT_NAME = "인사팀"
        private const val FIRST_STEP_REJECT_MESSAGE = "1차 결재 단계에서 반려되어 자동 반려 처리되었습니다."
    }
}
