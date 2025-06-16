package programmers.team6.domain.vacation.support

import java.time.LocalDateTime

@JvmRecord
data class MonthRange(
    val start: LocalDateTime,
    val end: LocalDateTime
)
