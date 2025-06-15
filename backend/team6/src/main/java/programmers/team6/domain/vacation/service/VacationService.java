package programmers.team6.domain.vacation.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.support.MonthRange;
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationListResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse;
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationUpdateResponseDto;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository;
import programmers.team6.domain.vacation.util.mapper.VacationMapper;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VacationService {

	private final VacationInfoRepository vacationInfoRepository;
	private final VacationMapper vacationMapper;

	private final VacationRequestRepository vacationRequestRepository;
	private final ApprovalStepRepository approvalStepRepository;

	private final MemberRepository memberRepository;
	private final CodeRepository codeRepository;

	private final ApprovalStepService approvalStepService;
	private final VacationRequestSearchRepository vacationRequestSearchRepository;

	// 본인 연차 조회
	@Transactional
	public VacationInfoSelectResponseDto getMyVacationInfo(Long memberId) {
		getMemberById(memberId);

		VacationInfo vacationInfo = vacationInfoRepository.findByMemberIdAndVacationType(memberId,
			VacationCode.ANNUAL.getCode()).orElseThrow(() -> new RuntimeException("휴가 정보를 찾을 수 없습니다."));

		return vacationMapper.toVacationInfoSelectResponseDto(vacationInfo);
	}

	// 휴가 신청
	@Transactional
	public VacationCreateResponseDto requestVacation(Long memberId, VacationCreateRequestDto requestDto) {

		// 신청자 정보 조회
		Member member = memberRepository.findByIdWithDeptAndLeader(memberId)
			.orElseThrow(() -> new RuntimeException("멤버 정보를 찾을 수 없습니다."));

		// 시작일(from)과 종료일(to) 설정 (진행중이거나 승인된 휴가 기간내에 신청 불가능하게)
		if (0 != vacationRequestRepository.countInRangeFromBetweenToBy(memberId, requestDto.getFrom(),
			requestDto.getTo())) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_VACATION_OVERLAP);
		}
		// 신청하려는 휴가 일수 계산
		double requestDays = vacationRequestRepository.calculateRequestedVacationDays(requestDto.getFrom(),
			requestDto.getTo(), requestDto.getVacationType());

		double actualRemainCount = vacationInfoRepository
			.findActualRemainingVacationDays(memberId, getVacationInfoType(requestDto.getVacationType()))
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_INFO));

		// 잔여 일수 초과 검증
		if (actualRemainCount < requestDays) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_INSUFFICIENT_VACATION_DAYS);
		}

		// 부서장 조회 (결재자)
		Dept dept = member.getDept();
		Member approver = dept.getDeptLeader();

		// 휴가 유형 코드 조회
		Code vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", requestDto.getVacationType())
			.orElseThrow(() -> new RuntimeException("잘못된 휴가 유형입니다."));

		// 휴가 요청 상태 코드 (기본 대기 상태)
		VacationRequestStatus status = VacationRequestStatus.IN_PROGRESS;

		// 휴가 요청 생성
		VacationRequest vacationRequest = vacationMapper.toVacationRequest(requestDto, vacationType, status, member);

		// 저장
		vacationRequestRepository.save(vacationRequest);

		// 결재 단계 생성
		approvalStepService.saveApprovalStep(approver, vacationRequest);

		// 응답 DTO 생성
		return vacationMapper.toVacationCreateResponseDto(
			vacationRequest,
			vacationType.getName(),
			status,
			approver.getName()
		);
	}

	// 휴가 신청 내역 (페이징: 20개씩)
	@Transactional(readOnly = true)
	public VacationListResponseDto getVacationRequestList(Long memberId, int page) {
		// 사용자 존재 여부 확인
		getMemberById(memberId);

		// 페이지 요청 객체 생성 (페이지 번호는 0부터 시작)
		Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"));

		// 1. ID만 페이징해서 가져오기
		Page<Long> idPage = vacationRequestRepository.findIdsByRequesterIdPaging(memberId, pageable);

		if (idPage.isEmpty()) {
			// 빈 결과 반환
			return VacationListResponseDto.builder()
				.content(Collections.emptyList())
				.pageNumber(page)
				.pageSize(pageable.getPageSize())
				.totalElements(idPage.getTotalElements())
				.totalPages(idPage.getTotalPages())
				.first(idPage.isFirst())
				.last(idPage.isLast())
				.build();
		}

		// 2. 페이징된 ID로 상세 정보 조회 (페치 조인 사용)
		List<VacationRequest> vacationRequests = vacationRequestRepository.findByIdsWithFetch(idPage.getContent());

		// 3. 결재 단계 정보 일괄 조회
		List<Long> requestIds = vacationRequests.stream()
			.map(VacationRequest::getId)
			.collect(Collectors.toList());

		Map<Long, ApprovalStep> approvalStepMap = approvalStepRepository.findFirstStepsByVacationRequestIds(requestIds)
			.stream()
			.collect(Collectors.toMap(
				step -> step.getVacationRequest().getId(),
				step -> step,
				(existing, replacement) -> existing
			));

		// 4. DTO 변환
		List<VacationCreateResponseDto> content = vacationRequests.stream()
			.map(request -> {
				ApprovalStep approvalStep = approvalStepMap.get(request.getId());
				String approverName = approvalStep != null ? approvalStep.getMember().getName() : "미지정";

				return vacationMapper.toVacationCreateResponseDto(
					request,
					request.getType().getName(),
					request.getStatus(),
					approverName
				);
			})
			.collect(Collectors.toList());

		// 5. 페이징 응답 DTO 생성
		return vacationMapper.toVacationListResponseDto(
			new PageImpl<>(vacationRequests, pageable, idPage.getTotalElements()),
			content
		);
	}

	// 휴가 신청 수정
	@Transactional
	public VacationUpdateResponseDto updateVacationRequest(Long memberId, Long requestId,
		VacationUpdateRequestDto requestDto) {
		// 요청자 확인
		getMemberById(memberId);

		// 휴가 신청 조회
		VacationRequest vacationRequest = vacationRequestRepository.findById(requestId)
			.orElseThrow(() -> new RuntimeException("휴가 신청 정보를 찾을 수 없습니다."));

		// 시작일(from)과 종료일(to) 설정 (진행중이거나 승인된 휴가 기간내에 신청 불가능하게) -> 수정할 휴가 내역을 수정할 때 발생하는 오류 잡기 위함
		if (0 != vacationRequestRepository.countInRangeFromBetweenToByExcludeRequestId(
			memberId, requestDto.getFrom(), requestDto.getTo(), requestId)) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_VACATION_OVERLAP);
		}

		// 신청하려는 휴가 일수 계산
		double requestDays = vacationRequestRepository.calculateRequestedVacationDays(requestDto.getFrom(),
			requestDto.getTo(), requestDto.getVacationType());

		// 실제 사용 가능한 잔여 휴가 일수를 한 번에 조회
		double actualRemainCount = vacationInfoRepository
			.findActualRemainingVacationDaysExcludeRequestId(memberId,
				getVacationInfoType(requestDto.getVacationType()), requestId)
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_INFO));

		// 잔여 일수 초과 검증
		if (actualRemainCount < requestDays) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_INSUFFICIENT_VACATION_DAYS);
		}

		// 휴가 유형 코드 조회
		Code vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", requestDto.getVacationType())
			.orElseThrow(() -> new RuntimeException("잘못된 휴가 유형입니다."));

		// 수정 권한 검증 및 수정 처리
		vacationRequest.updateByMember(memberId, requestDto.getFrom(), requestDto.getTo(), requestDto.getReason(),
			vacationType);

		// 결재자 정보 조회
		ApprovalStep approvalStep = approvalStepRepository.findFirstByVacationRequestOrderByStepAsc(
				vacationRequest)
			.orElseThrow(() -> new RuntimeException("결재 단계 정보를 찾을 수 없습니다."));

		// 응답 DTO 생성
		return vacationMapper.toVacationUpdateResponseDto(
			vacationRequest,
			vacationType.getName(),
			approvalStep.getMember().getName()
		);
	}

	// 대기중인 휴가 신청 취소
	@Transactional
	public boolean cancelVacationRequest(Long memberId, Long requestId) {
		// 멤버 존재 여부 먼저 확인
		getMemberById(memberId);

		// 휴가 신청 조회
		VacationRequest vacationRequest = vacationRequestRepository.findById(requestId)
			.orElseThrow(() -> new RuntimeException("휴가 신청 정보를 찾을 수 없습니다."));

		// 휴가 신청 취소
		vacationRequest.validateAndCancel(memberId);

		// 1,2차 결재 취소
		approvalStepService.cancelApprovalStep(vacationRequest);

		return true;
	}

	@Transactional(readOnly = true)
	public List<VacationRequestCalendarResponse> selectVacationCalendar(String yearMonthStr, Long deptId) {

		MonthRange monthRange = getMonthRange(yearMonthStr);

		return vacationRequestSearchRepository.findApprovedVacationsByMonth(VacationRequestStatus.APPROVED,
			monthRange.start(),
			monthRange.end(),
			deptId);
	}

	private MonthRange getMonthRange(String yearMonthStr) {
		YearMonth yearMonth = YearMonth.parse(yearMonthStr);

		LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
		LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

		return new MonthRange(start, end);
	}

	// 멤버 ID로 멤버를 조회, 멤버가 존재하지 않으면 예외 발생
	private Member getMemberById(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> new RuntimeException("멤버 정보를 찾을 수 없습니다."));
	}

	// 반차 코드(05)를 01로 변환
	private String getVacationInfoType(String type) {
		if (type.equals("05")) {
			return "01";
		}
		return type;
	}

}