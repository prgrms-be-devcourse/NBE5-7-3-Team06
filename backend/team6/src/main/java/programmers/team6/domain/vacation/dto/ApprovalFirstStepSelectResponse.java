package programmers.team6.domain.vacation.dto;

import java.time.LocalDateTime;

import programmers.team6.domain.vacation.enums.ApprovalStatus;

public record ApprovalFirstStepSelectResponse(
	Long approvalStepId,
	String type,
	LocalDateTime from,
	LocalDateTime to,
	String name,
	String deptName,
	String positionName,
	ApprovalStatus status
) {
}
