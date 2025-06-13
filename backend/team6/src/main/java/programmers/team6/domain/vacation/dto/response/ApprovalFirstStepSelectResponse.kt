package programmers.team6.domain.vacation.dto.response

import programmers.team6.domain.vacation.enums.ApprovalStatus
import java.time.LocalDateTime

data class ApprovalFirstStepSelectResponse(
    val approvalStepId: Long,
    val type: String,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val name: String,
    val deptName: String,
    val positionName: String,
    val status: ApprovalStatus
)
