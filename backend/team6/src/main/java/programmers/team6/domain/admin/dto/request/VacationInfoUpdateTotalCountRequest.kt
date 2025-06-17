package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class VacationInfoUpdateTotalCountRequest(
    @field:NotNull @field:PositiveOrZero
    val id: Int,
    @field:NotNull @field:PositiveOrZero
    val totalCount: Double,
    @field:NotNull
    val type: String,
    @field:NotNull @field:PositiveOrZero
    val version: Int
) {
    fun isSameType(type: String): Boolean {
        return this.type == type
    }
}
