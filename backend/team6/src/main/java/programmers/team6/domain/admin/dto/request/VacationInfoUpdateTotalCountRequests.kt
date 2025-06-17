package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.util.*

data class VacationInfoUpdateTotalCountRequests(
    val memberId: @NotNull @PositiveOrZero Long?,
    val vacations: @NotNull @NotEmpty MutableList<VacationInfoUpdateTotalCountRequest>?
) {
    val ids: List<Int>
        get() = vacations!!.stream().map(VacationInfoUpdateTotalCountRequest::id).toList()

    fun getTarget(type: String?): Optional<VacationInfoUpdateTotalCountRequest> {
        for (vacation in vacations!!) {
            if (vacation.isSameType(type)) {
                return Optional.of(vacation)
            }
        }
        return Optional.empty()
    }
}
