package programmers.team6.domain.admin.enums

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

enum class Quarter(private val value: Int) {
    Q1(1), Q2(2), Q3(3), Q4(4), H1(5), H2(6), NONE(0);

    fun getStart(year: Int): LocalDateTime {
        val startDate = when (this) {
            Q1, Q2, Q3, Q4 -> applyQuarterAdjuster(
                year,
                TemporalAdjusters.firstDayOfMonth()
            )

            H1, NONE -> LocalDate.of(year, Month.JANUARY, 1)
            H2 -> LocalDate.of(year, Month.JULY, 1)
        }
        return startDate.atStartOfDay()
    }

    fun getEnd(year: Int): LocalDateTime {
        val endDate = when (this) {
            Q1, Q2, Q3, Q4 -> applyQuarterAdjuster(
                year,
                TemporalAdjusters.lastDayOfMonth()
            ).plusMonths(2)

            H1 -> LocalDate.of(year, Month.JUNE, 30)
            H2, NONE -> LocalDate.of(year, Month.DECEMBER, 31)
        }

        return endDate.atTime(23, 59, 59)
    }

    private fun applyQuarterAdjuster(year: Int, temporalAdjuster: TemporalAdjuster): LocalDate {
        return LocalDate.of(year, 1, 1).with(IsoFields.QUARTER_OF_YEAR, this.value.toLong()).with(temporalAdjuster)
    }
}
