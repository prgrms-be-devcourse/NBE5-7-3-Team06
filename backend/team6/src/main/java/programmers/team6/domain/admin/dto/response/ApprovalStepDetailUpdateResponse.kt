package programmers.team6.domain.admin.dto.response

import programmers.team6.domain.vacation.enums.ApprovalStatus

data class ApprovalStepDetailUpdateResponse(
    val name: String,
    val reason: String,
    val approvalStatus: ApprovalStatus
)
