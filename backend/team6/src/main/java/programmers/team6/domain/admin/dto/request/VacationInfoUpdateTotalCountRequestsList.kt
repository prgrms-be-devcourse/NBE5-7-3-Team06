package programmers.team6.domain.admin.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VacationInfoUpdateTotalCountRequestsList(
	@NotEmpty @NotNull List<@Valid VacationInfoUpdateTotalCountRequests> requests) {

	public List<Integer> vacationIds() {
		return requests.stream()
			.map(VacationInfoUpdateTotalCountRequests::getIds)
			.flatMap(List::stream)
			.toList();
	}
}
