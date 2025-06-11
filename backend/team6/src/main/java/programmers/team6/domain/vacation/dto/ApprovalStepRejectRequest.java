package programmers.team6.domain.vacation.dto;

import jakarta.validation.constraints.NotBlank;

public record ApprovalStepRejectRequest(
	@NotBlank(message = "사유를 입력해주세요.")
	String reason
) {
}
