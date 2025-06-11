package programmers.team6.domain.vacation.dto;

import java.time.LocalDateTime;

public record VacationRequestCalendarResponse(
	String name,
	String deptName,
	String typeName,
	String positionName,
	LocalDateTime from,
	LocalDateTime to) {
}
