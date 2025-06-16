package programmers.team6.domain.vacation.dto.request

import jakarta.validation.constraints.NotBlank

data class ApprovalStepRejectRequest(
    @field:NotBlank(message = "사유를 입력해주세요.")
    val reason: String
)
