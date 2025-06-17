package programmers.team6.domain.vacation.dto.response

import java.time.LocalDateTime

data class VacationRequestCalendarResponse(
    val name: String,
    val deptName: String,
    val typeName: String,
    val positionName: String,
    val from: LocalDateTime,
    val to: LocalDateTime
)
