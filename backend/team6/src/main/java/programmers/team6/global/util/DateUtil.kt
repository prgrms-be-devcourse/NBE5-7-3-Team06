
package programmers.team6.global.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

	public static boolean isEqualsOrBefore(LocalDate left, LocalDate right) {
		return left.isBefore(right) || left.isEqual(right);
	}

	public static int calcYearsOfService(LocalDate now, LocalDate joinDate) {
		Period period = Period.between(joinDate, now);
		return Math.abs(period.getYears());
	}

	public static int calcDaysOfService(LocalDate now, LocalDate joinDate) {
		return (int) Math.abs(ChronoUnit.DAYS.between(joinDate, now));
	}

	public static LocalDate lastDateFrom(LocalDate date){
		return date.withDayOfMonth(date.lengthOfMonth());
	}

	public static LocalDate startDateFrom(LocalDate date){
		return date.withDayOfMonth(1);
	}
}