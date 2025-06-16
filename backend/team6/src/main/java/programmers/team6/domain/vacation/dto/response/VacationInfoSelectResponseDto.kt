package programmers.team6.domain.vacation.dto.response

data class VacationInfoSelectResponseDto(
    val totalCount: Double,
    val useCount: Double
) {

    val remainCount: Double
        get() = totalCount - useCount

}
