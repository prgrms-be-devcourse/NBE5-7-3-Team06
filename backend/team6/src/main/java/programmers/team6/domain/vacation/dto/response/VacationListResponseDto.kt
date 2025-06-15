package programmers.team6.domain.vacation.dto.response

data class VacationListResponseDto(
    val content: List<VacationCreateResponseDto>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)
