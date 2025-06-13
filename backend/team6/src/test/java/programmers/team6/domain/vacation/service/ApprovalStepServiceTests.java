package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.params.provider.EnumSource.Mode.*;
import static org.mockito.Mockito.*;
import static programmers.team6.domain.vacation.service.util.ApprovalStepServiceUtils.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.dto.request.ApprovalStepRejectRequest;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.support.MemberMother;

@ExtendWith(MockitoExtension.class)
class ApprovalStepServiceTests {

	@Mock
	private ApprovalStepRepository approvalStepRepository;

	@Mock
	private VacationInfoRepository vacationInfoRepository;

	@Mock
	private VacationInfoLogPublisher vacationInfoLogPublisher;

	@InjectMocks
	private ApprovalStepService approvalStepService;

	@Test
	@DisplayName("1차 결재에서 1차 결재자가 인사담당자가 아닌 경우 승인 시 1차 APPROVED, 2차 PENDING")
	void approveFirstStep_nonHrApprover_success() {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		Long secondStepId = 2L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest);
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));
		when(approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2))
			.thenReturn(Optional.of(secondApprovalStep));

		// when
		approvalStepService.approveFirstStep(firstStepId, firstApproverId);

		//then
		assertThat(firstApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);

	}

	@Test
	@DisplayName("1차 결재에서 1차 결재자가 인사담당자인 경우 승인 시 1차 APPROVED, 2차 APPROVED")
	void approveFirstStep_HrApprover_success() {
		// given
		Long memberId = 1L;
		Long hrApproverId = 2L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member hrApprover = MemberMother.withIdAndDeptName(hrApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);
		VacationInfo vacationInfo = new VacationInfo(15, "01", memberId);

		Long firstStepId = 1L;
		Long secondStepId = 2L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, hrApprover, vacationRequest);
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, hrApprover, vacationRequest);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, hrApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));
		when(approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2))
			.thenReturn(Optional.of(secondApprovalStep));
		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, hrApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));
		when(vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01"))
			.thenReturn(Optional.of(vacationInfo));
		doNothing().when(vacationInfoLogPublisher).publish(any(VacationInfoLog.class));

		// when
		approvalStepService.approveFirstStep(firstStepId, hrApproverId);

		//then
		assertThat(firstApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);

	}

	@Test
	@DisplayName("1차 승인에서 해당 1차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void approveFirstStep_findFirstStep_notFound_throwsNotFoundException() {

		// given
		Long failedNum = 99999L;
		Long firstApproverId = 2L;
		when(approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, firstApproverId, 1))
			.thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveFirstStep(failedNum, firstApproverId);
			}
		).isInstanceOf(NotFoundException.class);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("1차 승인에서 해당 1차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
	void approveFirstStep_firstStepStatus_notPending_throwsBadRequestException(ApprovalStatus status) {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest, status);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveFirstStep(firstStepId, firstApproverId);
			}
		).isInstanceOf(BadRequestException.class);

	}

	@Test
	@DisplayName("1차 승인에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void approveFirstStep_findSecondStep_notFound_throwsNotFoundException() {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));
		when(approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2))
			.thenReturn(Optional.empty());

		//then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveFirstStep(firstStepId, firstApproverId);
			}
		).isInstanceOf(NotFoundException.class);

	}

	@Test
	@DisplayName("1차 결재에서 반려 시 1차 REJECTED, 2차 REJECTED, 휴가 REJECTED")
	void rejectFirstStep_success() {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		Long secondStepId = 2L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest);
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));
		when(approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2))
			.thenReturn(Optional.of(secondApprovalStep));

		// when
		approvalStepService.rejectFirstStep(firstStepId, firstApproverId, new ApprovalStepRejectRequest("안됨"));

		//then
		assertThat(firstApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.REJECTED);

	}

	@Test
	@DisplayName("1차 반려에서 해당 1차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void rejectFirstStep_findFirstStep_notFound_throwsNotFoundException() {

		// given
		Long failedNum = 99999L;
		Long firstApproverId = 2L;
		when(approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, firstApproverId, 1))
			.thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.rejectFirstStep(failedNum, firstApproverId, new ApprovalStepRejectRequest("안됨"));
			}
		).isInstanceOf(NotFoundException.class);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("1차 반려에서 해당 1차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
	void rejectFirstStep_firstStepStatus_notPending_throwsBadRequestException(ApprovalStatus status) {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest, status);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.rejectFirstStep(firstStepId, firstApproverId, new ApprovalStepRejectRequest("안됨"));
			}
		).isInstanceOf(BadRequestException.class);

	}

	@Test
	@DisplayName("1차 결재에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void rejectFirstStep_findSecondStep_notFound_throwsNotFoundException() {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(firstStepId, firstApproverId, 1))
			.thenReturn(Optional.of(firstApprovalStep));
		when(approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2))
			.thenReturn(Optional.empty());

		//then
		assertThatThrownBy(
			() -> {
				approvalStepService.rejectFirstStep(firstStepId, firstApproverId, new ApprovalStepRejectRequest("안됨"));
			}
		).isInstanceOf(NotFoundException.class);

	}

	@Test
	@DisplayName("2차 결재 승인 시 2차 APPROVED, 휴가 APPROVED, 휴가 일수 차감")
	void approveSecondStep_success() {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);
		VacationInfo vacationInfo = new VacationInfo(15, "01", memberId);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest,
			ApprovalStatus.PENDING);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));
		when(vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01"))
			.thenReturn(Optional.of(vacationInfo));
		doNothing().when(vacationInfoLogPublisher).publish(any(VacationInfoLog.class));

		// when
		boolean tf = approvalStepService.approveSecondStep(secondStepId, secondApproverId);

		//then
		assertThat(tf).isEqualTo(true);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.APPROVED);
		assertThat(vacationInfo.getUseCount()).isEqualTo(3);

	}

	@Test
	@DisplayName("2차 승인에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void approveSecondStep_findSecondStep_notFound_throwsNotFoundException() {

		// given
		Long failedNum = 99999L;
		Long secondApproverId = 2L;
		when(approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, secondApproverId, 2))
			.thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveSecondStep(failedNum, secondApproverId);
			}
		).isInstanceOf(NotFoundException.class);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("2차 승인에서 해당 2차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
	void approveSecondStep_secondStepStatus_notPending_throwsBadRequestException(ApprovalStatus status) {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genFirstStep(secondStepId, secondApprover, vacationRequest, status);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveSecondStep(secondStepId, secondApproverId);
			}
		).isInstanceOf(BadRequestException.class);

	}

	@Test
	@DisplayName("2차 승인에서 휴가정보를 찾을 수 없는 경우 NotFoundException 예외 발생")
	void approveSecondStep_findVacationInfo_notFound_throwsNotFoundException() {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest,
			ApprovalStatus.PENDING);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));
		when(vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01"))
			.thenReturn(Optional.empty());

		//then
		assertThatThrownBy(
			() -> {
				approvalStepService.approveSecondStep(secondStepId, secondApproverId);
			}
		).isInstanceOf(NotFoundException.class);

	}

	@Test
	@DisplayName("2차 승인에서 총 휴가 일수를 넘긴 경우 2차 CANCELED, 휴가 CANCELED")
	void approveSecondStep_cancel() {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);
		VacationInfo vacationInfo = new VacationInfo(15, 13, "01", memberId);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest,
			ApprovalStatus.PENDING);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));
		when(vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01"))
			.thenReturn(Optional.of(vacationInfo));

		// when
		boolean tf = approvalStepService.approveSecondStep(secondStepId, secondApproverId);

		//then
		assertThat(tf).isEqualTo(false);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.CANCELED);
		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.CANCELED);

	}

	@Test
	@DisplayName("2차 결재 반려 시 2차 REJECTED, 휴가 REJECTED")
	void rejectSecondStep_success() {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest,
			ApprovalStatus.PENDING);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));

		// when
		approvalStepService.rejectSecondStep(secondStepId, secondApproverId, new ApprovalStepRejectRequest("안됨"));

		//then
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
		assertThat(vacationRequest.getStatus()).isEqualTo(VacationRequestStatus.REJECTED);

	}

	@Test
	@DisplayName("2차 반려에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
	void rejectSecondStep_findSecondStep_notFound_throwsNotFoundException() {

		// given
		Long failedNum = 99999L;
		Long secondApproverId = 2L;
		when(approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, secondApproverId, 2))
			.thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.rejectSecondStep(failedNum, secondApproverId, new ApprovalStepRejectRequest("안됨"));
			}
		).isInstanceOf(NotFoundException.class);
	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("2차 반려에서 해당 2차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
	void rejectSecondStep_secondStepStatus_notPending_throwsBadRequestException(ApprovalStatus status) {
		// given
		Long memberId = 1L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long secondStepId = 2L;
		ApprovalStep secondApprovalStep = genFirstStep(secondStepId, secondApprover, vacationRequest, status);

		when(approvalStepRepository.findByIdAndMemberIdAndStep(secondStepId, secondApproverId, 2))
			.thenReturn(Optional.of(secondApprovalStep));

		// then
		assertThatThrownBy(
			() -> {
				approvalStepService.rejectSecondStep(secondStepId, secondApproverId,
					new ApprovalStepRejectRequest("안됨"));
			}
		).isInstanceOf(BadRequestException.class);

	}

	@Test
	@DisplayName("휴가 취소 시 1차 CANCELED, 2차 CANCELED")
	void cancelApprovalStep_success() {
		// given
		Long memberId = 1L;
		Long firstApproverId = 2L;
		Long secondApproverId = 3L;

		Member member = MemberMother.withIdAndDeptName(memberId, "개발팀");
		Member firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀");
		Member secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀");

		VacationRequest vacationRequest = genVacationRequest(member);

		Long firstStepId = 1L;
		Long secondStepId = 2L;
		ApprovalStep firstApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest);
		ApprovalStep secondApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest);

		when(approvalStepRepository.findByVacationRequest(vacationRequest))
			.thenReturn(List.of(firstApprovalStep, secondApprovalStep));

		// when
		approvalStepService.cancelApprovalStep(vacationRequest);

		//then
		assertThat(firstApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.CANCELED);
		assertThat(secondApprovalStep.getApprovalStatus()).isEqualTo(ApprovalStatus.CANCELED);

	}

}