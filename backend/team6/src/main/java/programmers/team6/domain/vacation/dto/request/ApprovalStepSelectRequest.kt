package programmers.team6.domain.vacation.dto.request

import programmers.team6.domain.vacation.enums.ApprovalStatus
import java.time.LocalDateTime

data class ApprovalStepSelectRequest(
    val type: String?,
    val name: String?,
    val from: LocalDateTime?,
    val to: LocalDateTime?,
    val status: ApprovalStatus?
) {
    fun hasFilter(): Boolean {
        return type != null || name != null || from != null || to != null || status != null
    }
}
