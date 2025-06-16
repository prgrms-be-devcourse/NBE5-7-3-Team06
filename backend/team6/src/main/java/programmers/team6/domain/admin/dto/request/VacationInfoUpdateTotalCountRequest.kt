package programmers.team6.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VacationInfoUpdateTotalCountRequest(@NotNull @PositiveOrZero Integer id,
												  @NotNull @PositiveOrZero Double totalCount,
												  @NotNull String type, @NotNull @PositiveOrZero Integer version) {

	public boolean isSameType(String type) {
		return this.type.equals(type);
	}
}
