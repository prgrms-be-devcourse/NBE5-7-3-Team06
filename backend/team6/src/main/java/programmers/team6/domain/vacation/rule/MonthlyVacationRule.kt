package programmers.team6.domain.vacation.rule

import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.global.entity.Positive
import java.time.LocalDate

private val STATUTORY_GRANT_DAYS = Positive(1)

class MonthlyVacationRule(private val grantDays: Positive) {

    fun grant(vacationInfo: VacationInfo): VacationInfoLog {
        return vacationInfo.updateTotalCount(vacationInfo.totalCount + grantDays.toInt())
    }

    fun getBaseLineDates(boundaryDate: LocalDate, now: LocalDate): List<LocalDate> {
        val baseDay = boundaryDate.dayOfMonth
        var current = boundaryDate.plusMonths(1).withDayOfMonth(1)

        val result = mutableListOf<LocalDate>()

        while (current.isBefore(now)) {
            val lastDayOfMonth = current.lengthOfMonth()
            val dayToUse = minOf(baseDay, lastDayOfMonth)
            val candidate = current.withDayOfMonth(dayToUse)

            if (candidate == now) break

            result += candidate

            if (isLastDayOfMonth(now) && now.lengthOfMonth() < candidate.lengthOfMonth()) {
                result += (baseDay + 1..lastDayOfMonth).map { day ->
                    candidate.withDayOfMonth(day)
                }
            }

            current = current.plusMonths(1)
        }

        return result
    }

    private fun isLastDayOfMonth(now: LocalDate): Boolean = now.dayOfMonth == now.lengthOfMonth()

    companion object {

        fun statutory(): MonthlyVacationRule {
            return MonthlyVacationRule(STATUTORY_GRANT_DAYS)
        }
    }
}
