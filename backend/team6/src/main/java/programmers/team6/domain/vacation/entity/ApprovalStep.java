package programmers.team6.domain.vacation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.global.entity.BaseEntity;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalStep extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "approval_step_id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "vacation_request_id", nullable = false)
	private VacationRequest vacationRequest;

	@Column(name = "approval_status", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private ApprovalStatus approvalStatus;

	private int step;

	private String reason;

	public ApprovalStep(Member member, VacationRequest vacationRequest, ApprovalStatus approvalStatus, int step,
		String reason) {
		this.member = member;
		this.vacationRequest = vacationRequest;
		this.approvalStatus = approvalStatus;
		this.step = step;
		this.reason = reason;
	}

	@Builder
	public ApprovalStep(int step, ApprovalStatus approvalStatus, Member member, VacationRequest vacationRequest) {
		this.step = step;
		this.approvalStatus = approvalStatus;
		this.member = member;
		this.vacationRequest = vacationRequest;
	}

	public void update(String reason) {
		this.reason = reason;
	}

	private void updateStatus(ApprovalStatus approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public void approve() {
		updateStatus(ApprovalStatus.APPROVED);
	}

	public void reject() {
		updateStatus(ApprovalStatus.REJECTED);
	}

	public void reject(String reason) {
		updateStatus(ApprovalStatus.REJECTED);
		this.reason = reason;
	}

	public void pending() {
		updateStatus(ApprovalStatus.PENDING);
	}

	public void cancel() {
		updateStatus(ApprovalStatus.CANCELED);
	}

	// todo : approve()하고 결합하여 하나의 승인 로직으로 리펙토링
	public void validateApprovable() {
		if (this.approvalStatus != ApprovalStatus.PENDING) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_APPROVE);
		}
	}

	public void validateRejectable() {
		if (this.approvalStatus != ApprovalStatus.PENDING) {
			throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_REJECT);
		}
	}

	public Long getVacationMemberId() {
		return this.vacationRequest.getMemberId();
	}

	public String getVacationCode() {
		return this.vacationRequest.getCode();
	}

	public int calcVacationDays() {
		return this.vacationRequest.calcVacationDays();
	}

	public void approveVacation() {
		this.vacationRequest.approve();
	}

	public void rejectVacation() {
		this.vacationRequest.reject();
	}

	public void cancelVacation() {
		this.vacationRequest.cancel();
	}

	public boolean isHalfDay() {
		return this.vacationRequest.isHalfDay();
	}

	public boolean isHrApprover() {
		return this.member.isHrMember();
	}
}
