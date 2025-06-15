package programmers.team6.domain.vacation.dto.response

data class VacationMonthlyStatisticsResponse(
    val memberId: Long,
    val userName: String,
    val totalCount: Double,
    val usedCount: Double,
    val remainCount: Double,
    val january: Double,
    val february: Double,
    val march: Double,
    val april: Double,
    val may: Double,
    val june: Double,
    val july: Double,
    val august: Double,
    val september: Double,
    val october: Double,
    val november: Double,
    val december: Double
)
