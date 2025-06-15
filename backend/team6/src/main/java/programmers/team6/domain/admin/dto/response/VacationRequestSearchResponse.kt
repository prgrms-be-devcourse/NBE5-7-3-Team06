package programmers.team6.domain.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import programmers.team6.domain.vacation.enums.VacationRequestStatus;

public record VacationRequestSearchResponse(Long id, String type, LocalDateTime from, LocalDateTime to, String applicantName,
											List<String> approverNames, String deptName, VacationRequestStatus status) {
	public VacationRequestSearchResponse(Long id, String type, LocalDateTime from, LocalDateTime to, String applicantName,
		String approverNames, String deptName, VacationRequestStatus status) {
		this(id, type, from, to, applicantName, List.of(approverNames.split(",")), deptName, status);
	}

}
