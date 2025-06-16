package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class VacationInfoUpdateTotalCountRequests(
    @field:NotNull @field:PositiveOrZero
    val memberId: Long,
    @field:NotNull @field:NotEmpty
    val vacations: List<VacationInfoUpdateTotalCountRequest>
) {
    val ids: List<Int>
        get() = vacations.stream().map(VacationInfoUpdateTotalCountRequest::id).toList()

    fun getTarget(type: String): VacationInfoUpdateTotalCountRequest? = vacations.firstOrNull { it.isSameType(type) }

}
