package programmers.team6.domain.vacation.dto;

import java.time.LocalDateTime;

import programmers.team6.domain.vacation.enums.ApprovalStatus;

public record ApprovalSecondStepSelectResponse(
	Long approvalStepId,
	String type,
	LocalDateTime from,
	LocalDateTime to,
	String name,
	String deptName,
	String positionName,
	ApprovalStatus firstApprovalStatus,
	ApprovalStatus secondApprovalStatus
) {
}
