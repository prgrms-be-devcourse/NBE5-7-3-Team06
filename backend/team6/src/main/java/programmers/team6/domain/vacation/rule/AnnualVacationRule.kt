package programmers.team6.domain.vacation.rule;

import static programmers.team6.global.util.DateUtil.*;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.global.entity.Positive;

@RequiredArgsConstructor
public final class AnnualVacationRule {

	private static final Positive STATUTORY_BOUNDARY_YEAR = new Positive(1);
	private static final Positive STATUTORY_INCREASE_YEAR = new Positive(2);
	private static final Positive STATUTORY_INCREASE_DAYS = new Positive(1);
	private static final Positive STATUTORY_INITIAL_GRANT_DAYS = new Positive(15);
	private static final VacationCode TYPE = VacationCode.ANNUAL;

	private final Positive boundaryYear;
	private final Positive increaseYear;
	private final Positive vacationIncreaseDays;
	private final Positive initialGrantDays;

	public static AnnualVacationRule statutory() {
		return new AnnualVacationRule(STATUTORY_BOUNDARY_YEAR, STATUTORY_INCREASE_YEAR, STATUTORY_INCREASE_DAYS,
			STATUTORY_INITIAL_GRANT_DAYS);
	}

	public LocalDate getJoinDate(LocalDate date) {
		return date.minusYears(boundaryYear.toInt());
	}

	public VacationInfoLog grant(LocalDate date, Member member, VacationInfo vacationInfo) {
		int yearsOfService = calcYearsOfService(date, member.getJoinDate().toLocalDate());
		return vacationInfo.init(calcIncreaseDays(yearsOfService));
	}

	public VacationInfo vacationInfo(Long memberId) {
		return new VacationInfo(initialGrantDays.toInt(), TYPE.getCode(), memberId);
	}

	public List<LocalDate> getBaseLineDates(LocalDate date) {
		return List.of(date.minusYears(boundaryYear.toInt()));
	}

	public boolean isSameType(VacationCode vacationCode) {
		return TYPE == vacationCode;
	}

	public VacationCode getType() {
		return TYPE;
	}

	public boolean isTarget(LocalDate date, Member member) {
		LocalDate joinDate = getJoinDate(date);
		return joinDate.isBefore(member.getJoinDate().toLocalDate()) || joinDate.isEqual(
			member.getJoinDate().toLocalDate());
	}

	private int calcIncreaseDays(int yearsOfService) {
		return initialGrantDays.toInt() + calculateAdditionalVacationDays(yearsOfService);
	}

	private int calculateAdditionalVacationDays(int yearsOfService) {
		return (((yearsOfService - boundaryYear.toInt()) / increaseYear.toInt()) * vacationIncreaseDays.toInt());
	}
}
