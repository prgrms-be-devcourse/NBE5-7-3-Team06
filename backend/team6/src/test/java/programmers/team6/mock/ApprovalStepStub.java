package programmers.team6.mock;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;

public class ApprovalStepStub extends ApprovalStep {

	private Long id;

	public ApprovalStepStub(Long id, Member member, VacationRequest vacationRequest,
		ApprovalStatus approvalStatus, int step) {
		super(step, approvalStatus, member, vacationRequest);
		this.id = id;
	}

	@Override
	public Long getId() {
		return this.id;
	}

}
