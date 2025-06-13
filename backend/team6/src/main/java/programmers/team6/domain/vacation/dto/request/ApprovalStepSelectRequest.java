package programmers.team6.domain.vacation.dto.request;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.vacation.enums.ApprovalStatus;

@Slf4j
public record ApprovalStepSelectRequest(
	String type,
	String name,
	LocalDateTime from,
	LocalDateTime to,
	ApprovalStatus status
) {
	public boolean hasFilter() {
		return type != null || name != null || from != null || to != null || status != null;
	}
}
