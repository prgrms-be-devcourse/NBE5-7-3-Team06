package programmers.team6.domain.admin.dto.response;

import programmers.team6.domain.vacation.enums.ApprovalStatus;

public record ApprovalStepDetailUpdateResponse(String name, String reason, ApprovalStatus approvalStatus) {
}
