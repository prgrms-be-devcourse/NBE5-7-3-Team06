package programmers.team6.domain.admin.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import programmers.team6.domain.admin.dto.CodeInfo;
import programmers.team6.domain.admin.enums.Quarter;

public record AdminVacationRequestSearchResponse(Page<VacationRequestSearchResponse> vacationRequestSearchResponses,
												 DropdownContents dropdownContents) {
	public AdminVacationRequestSearchResponse(Page<VacationRequestSearchResponse> vacationRequestSearchResponses,
		List<CodeInfo> positionCodes, List<CodeInfo> vacationTypeCodes) {
		this(vacationRequestSearchResponses,
			new DropdownContents(positionCodes, vacationTypeCodes, List.of(Quarter.values())));
	}

	record DropdownContents(List<CodeInfo> positionCodes, List<CodeInfo> vacationTypeCodes, List<Quarter> quarters) {
	}
}
