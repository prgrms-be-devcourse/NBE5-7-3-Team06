package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.global.validator.EnumValue


data class VacationStatisticsRequest(
    @field:Positive val year: Int,
    val name: String?,
    val deptId: Long?,
    @field:EnumValue(
        enumClass = VacationCode::class,
        fieldName = "code"
    ) val vacationCode: @NotEmpty String
)
