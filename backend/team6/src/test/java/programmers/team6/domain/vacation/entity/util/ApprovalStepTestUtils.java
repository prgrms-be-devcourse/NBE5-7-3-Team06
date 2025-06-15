package programmers.team6.domain.vacation.entity.util;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;

public class ApprovalStepTestUtils {

	public static ApprovalStep genApprovalStep(ApprovalStatus status) {
		return new ApprovalStep(null, Member.builder().build(), VacationRequest.builder().build(), status, 1, null);
	}

}
