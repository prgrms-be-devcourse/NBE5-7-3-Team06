package programmers.team6.mock;

import java.time.LocalDate;
import java.util.List;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.support.TestVacationInfoBuilder;

public class VacationGrantRuleFake implements VacationGrantRule {

	private final VacationCode vacationCode;

	public VacationGrantRuleFake(VacationCode vacationCode) {
		this.vacationCode = vacationCode;
	}

	@Override
	public boolean canUpdate(double totalCount) {
		return true;
	}

	@Override
	public VacationInfo createVacationInfo(Long memberId) {
		return new TestVacationInfoBuilder()
			.memberId(memberId)
			.totalCount(15)
			.vacationType(vacationCode.getCode()).build();
	}

	@Override
	public boolean isSameType(VacationCode vacationCode) {
		return vacationCode == this.vacationCode;
	}

	@Override
	public List<LocalDate> getBaseLineDates(LocalDate date) {
		return List.of(date);
	}

	@Override
	public String getTypeCode() {
		return vacationCode.getCode();
	}

	@Override
	public VacationInfoLog grant(LocalDate date, Member member, VacationInfo info) {
		return info.init(10L);
	}
}
