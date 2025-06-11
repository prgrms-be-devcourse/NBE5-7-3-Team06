package programmers.team6.domain.vacation.rule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.global.entity.Positive;

@RequiredArgsConstructor
public final class MonthlyVacationRule {

	private static final Positive STATUTORY_GRANT_DAYS = new Positive(1);

	private final Positive grantDays;

	public static MonthlyVacationRule statutory() {
		return new MonthlyVacationRule(STATUTORY_GRANT_DAYS);
	}

	public VacationInfoLog grant(VacationInfo vacationInfo) {
		return vacationInfo.updateTotalCount(vacationInfo.getTotalCount() + grantDays.toInt());
	}

	public List<LocalDate> getBaseLineDates(LocalDate boundLineDate, LocalDate now) {

		List<LocalDate> result = new ArrayList<>();
		int baseDay = boundLineDate.getDayOfMonth();

		LocalDate current = boundLineDate.plusMonths(1).withDayOfMonth(1);

		while (current.isBefore(now)) {
			int lastDay = current.lengthOfMonth();
			int dayToUse = Math.min(baseDay, lastDay);
			LocalDate candidate = current.withDayOfMonth(dayToUse);

			if (candidate.isEqual(now)) {
				break;
			}
			result.add(candidate);

			if (isLastDays(now) && now.lengthOfMonth() < candidate.lengthOfMonth()) {
				for (int days = baseDay + 1; days <= lastDay; days++) {
					result.add(candidate.withDayOfMonth(days));
				}
			}

			current = current.plusMonths(1);
		}

		return result;
	}

	private boolean isLastDays(LocalDate now) {
		int day = now.getDayOfMonth();
		return day == now.lengthOfMonth();
	}
}
