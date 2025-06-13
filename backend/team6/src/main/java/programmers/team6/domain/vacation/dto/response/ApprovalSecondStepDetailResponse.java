package programmers.team6.domain.vacation.dto.response;

import java.time.LocalDateTime;

import programmers.team6.domain.vacation.enums.ApprovalStatus;

public record ApprovalSecondStepDetailResponse(
	Long approvalStepId,
	String name,
	String deptName,
	String positionName,
	ApprovalStatus status,
	String type,
	LocalDateTime from,
	LocalDateTime to,
	String reason,
	String approverName,
	String approvalReason
) {
}
