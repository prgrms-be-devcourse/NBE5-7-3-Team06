package programmers.team6.domain.admin.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import programmers.team6.domain.vacation.enums.VacationRequestStatus;

public record VacationRequestDetailReadResponse(Long id, LocalDateTime from, LocalDateTime to, Long memberId,
												String name,
												String deptName,
												String position, String reason, String vacationType,
												VacationRequestStatus vacationRequestStatus,
												List<ApprovalStepDetailUpdateResponse> approvalStepDetailUpdateResponses) {
	public VacationRequestDetailReadResponse(Long id, LocalDateTime from, LocalDateTime to, Long memberId, String name,
		String deptName,
		String position, String reason, String vacationType, VacationRequestStatus vacationRequestStatus) {
		this(id, from, to, memberId, name, deptName, position, reason, vacationType, vacationRequestStatus,
			new ArrayList<>());
	}

	public VacationRequestDetailReadResponse injectApprovalStepDetails(
		List<ApprovalStepDetailUpdateResponse> approvalStepDetailUpdateResponses) {
		this.approvalStepDetailUpdateResponses.addAll(approvalStepDetailUpdateResponses);
		return this;
	}
}
