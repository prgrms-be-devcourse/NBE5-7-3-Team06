package programmers.team6.domain.admin.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.global.validator.EnumValue;

public record VacationStatisticsRequest(@Positive Integer year, String name,
										Long deptId,
										@EnumValue(enumClass = VacationCode.class, fieldName = "code") @NotEmpty String vacationCode) {
}
