package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class VacationInfoUpdateTotalCountRequestsList(val requests: @NotEmpty @NotNull List<VacationInfoUpdateTotalCountRequests>?) {
    fun vacationIds(): List<Int> {
        return requests!!.stream()
            .map { obj: VacationInfoUpdateTotalCountRequests -> obj.ids }
            .flatMap { obj: List<Int> -> obj.stream() }
            .toList()
    }
}
