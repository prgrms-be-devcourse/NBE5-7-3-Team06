package programmers.team6.domain.vacation.dto.response
import java.time.LocalDateTime


data class VacationCreateResponseDto(
    val requestId: Long,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val reason: String,
    val vacationType: String,
    val approvalStatus: String,
    val approverName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)