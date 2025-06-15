package programmers.team6.domain.vacation.dto.response

data class VacationCancelResponseDto(
    val requestId: Long,
    val success: Boolean,
    val message: String
)