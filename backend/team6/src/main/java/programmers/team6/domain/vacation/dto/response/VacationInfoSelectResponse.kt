package programmers.team6.domain.vacation.dto.response

@JvmRecord
data class VacationInfoSelectResponse(
    val id: Int,
    val totalCount: Double,
    val vacationType: String,
    val version: Int
)
