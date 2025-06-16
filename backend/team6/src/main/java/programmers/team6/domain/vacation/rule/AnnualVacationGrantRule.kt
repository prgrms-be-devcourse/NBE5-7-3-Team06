package programmers.team6.domain.vacation.rule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.global.entity.Positive;

@RequiredArgsConstructor
public final class AnnualVacationGrantRule implements VacationGrantRule {

	private static final Positive STATUTORY_MAX_GRANT_DAYS = new Positive(25);

	private final AnnualVacationRule annualVacationRule;
	private final MonthlyVacationRule monthlyVacationRule;

	private final Positive maxGrantDays;

	public static AnnualVacationGrantRule statutory() {
		return new AnnualVacationGrantRule(AnnualVacationRule.statutory(), MonthlyVacationRule.statutory(),
			STATUTORY_MAX_GRANT_DAYS);
	}

	@Override
	public boolean canUpdate(double totalCount) {
		return this.maxGrantDays.toInt() >= totalCount;
	}

	@Override
	public VacationInfo createVacationInfo(Long memberId) {
		return annualVacationRule.vacationInfo(memberId);
	}

	@Override
	public boolean isSameType(VacationCode vacationCode) {
		return annualVacationRule.isSameType(vacationCode);
	}

	@Override
	public List<LocalDate> getBaseLineDates(LocalDate date) {
		ArrayList<LocalDate> result = new ArrayList<>();
		result.addAll(annualVacationRule.getBaseLineDates(date));
		result.addAll(monthlyVacationRule.getBaseLineDates(annualVacationRule.getJoinDate(date), date));
		return result;
	}

	@Override
	public String getTypeCode() {
		return annualVacationRule.getType().getCode();
	}

	@Override
	public VacationInfoLog grant(LocalDate date, Member member, VacationInfo info) {
		if (annualVacationRule.isTarget(date, member)) {
			return grantAnnual(date, member, info);
		}
		return grantMonthly(info);
	}

	private VacationInfoLog grantAnnual(LocalDate date, Member member, VacationInfo vacationInfo) {
		VacationInfoLog log = annualVacationRule.grant(date, member, vacationInfo);
		if (maxGrantDays.toInt() < vacationInfo.getTotalCount()) {
			return vacationInfo.updateTotalCount(maxGrantDays.toInt());
		}
		return log;
	}

	private VacationInfoLog grantMonthly(VacationInfo vacationInfo) {
		VacationInfoLog log = monthlyVacationRule.grant(vacationInfo);
		if (maxGrantDays.toInt() < vacationInfo.getTotalCount()) {
			return vacationInfo.updateTotalCount(maxGrantDays.toInt());
		}
		return log;
	}
}
