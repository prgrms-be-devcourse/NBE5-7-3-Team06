package programmers.team6.domain.vacation.service.util;

import java.time.LocalDateTime;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.mock.ApprovalStepStub;
import programmers.team6.support.VacationTypeMother;

public class ApprovalStepServiceUtils {
	public static VacationRequest genVacationRequest(Member member) {
		return VacationRequest.builder()
			.member(member)
			.from(LocalDateTime.of(2025, 8, 1, 9, 0))
			.to(LocalDateTime.of(2025, 8, 3, 18, 0))
			.reason("사정이 있습니다.")
			.type(VacationTypeMother.Annual())
			.status(null)
			.build();
	}

	public static ApprovalStep genFirstStep(Long id, Member approver, VacationRequest vacationRequest) {
		return new ApprovalStepStub(id, approver, vacationRequest,
			ApprovalStatus.PENDING, 1);
	}

	public static ApprovalStep genFirstStep(Long id, Member approver, VacationRequest vacationRequest,
		ApprovalStatus status) {
		return new ApprovalStepStub(id, approver, vacationRequest, status, 1);
	}

	public static ApprovalStep genSecondStep(Long id, Member approver, VacationRequest vacationRequest) {
		return new ApprovalStepStub(id, approver, vacationRequest,
			ApprovalStatus.WAITING, 2);
	}

	public static ApprovalStep genSecondStep(Long id, Member approver, VacationRequest vacationRequest,
		ApprovalStatus status) {
		return new ApprovalStepStub(id, approver, vacationRequest, status, 2);
	}
}
