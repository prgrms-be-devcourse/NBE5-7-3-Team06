package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class VacationInfoUpdateTotalCountRequestsList(
    @field:NotEmpty @field:NotNull
    val requests: List<VacationInfoUpdateTotalCountRequests>
) {
    fun vacationIds(): List<Int> = requests.map { it.ids }.flatten()
}
