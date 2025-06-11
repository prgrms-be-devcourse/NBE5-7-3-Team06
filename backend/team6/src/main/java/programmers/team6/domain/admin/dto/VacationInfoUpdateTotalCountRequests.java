package programmers.team6.domain.admin.dto;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VacationInfoUpdateTotalCountRequests(
	@NotNull @PositiveOrZero Long memberId,
	@NotNull @NotEmpty List<@Valid VacationInfoUpdateTotalCountRequest> vacations) {

	public List<Integer> getIds() {
		return vacations.stream().map(VacationInfoUpdateTotalCountRequest::id).toList();
	}

	public Optional<VacationInfoUpdateTotalCountRequest> getTarget(String type) {
		for (VacationInfoUpdateTotalCountRequest vacation : vacations) {
			if (vacation.isSameType(type)) {
				return Optional.of(vacation);
			}
		}
		return Optional.empty();
	}
}
