package programmers.team6.domain.vacation.dto.request

import programmers.team6.domain.vacation.enums.ApprovalStatus
import java.time.LocalDateTime

data class ApprovalStepSelectRequest(
    val type: String? = null,
    val name: String? = null,
    val from: LocalDateTime? = null,
    val to: LocalDateTime? = null,
    val status: ApprovalStatus? = null,
) {
    fun hasFilter(): Boolean {
        return type != null || name != null || from != null || to != null || status != null
    }
}
