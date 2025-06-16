package programmers.team6.global.util

import lombok.AccessLevel
import lombok.NoArgsConstructor
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@NoArgsConstructor(access = AccessLevel.PRIVATE)
object DateUtil {
    @JvmStatic
	fun isEqualsOrBefore(left: LocalDate, right: LocalDate): Boolean {
        return left.isBefore(right) || left.isEqual(right)
    }

    @JvmStatic
	fun calcYearsOfService(now: LocalDate, joinDate: LocalDate): Int {
        val period = Period.between(joinDate, now)
        return abs(period.getYears())
    }

    fun calcDaysOfService(now: LocalDate, joinDate: LocalDate): Int {
        return abs(ChronoUnit.DAYS.between(joinDate, now)).toInt()
    }

    fun lastDateFrom(date: LocalDate): LocalDate {
        return date.withDayOfMonth(date.lengthOfMonth())
    }

    fun startDateFrom(date: LocalDate): LocalDate {
        return date.withDayOfMonth(1)
    }
}