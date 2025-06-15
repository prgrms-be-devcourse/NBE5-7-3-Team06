package programmers.team6.domain.admin.dto.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.global.validator.EnumValue

@JvmRecord
data class VacationStatisticsRequest(
    @JvmField @field:Positive val year: Int,
    @JvmField val name: String?,
    @JvmField val deptId: Long?,
    @JvmField @field:EnumValue(
        enumClass = VacationCode::class,
        fieldName = "code"
    ) val vacationCode: @NotEmpty String
)
