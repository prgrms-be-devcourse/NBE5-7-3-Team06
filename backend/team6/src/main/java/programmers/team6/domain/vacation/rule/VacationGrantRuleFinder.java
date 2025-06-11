package programmers.team6.domain.vacation.rule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;

@Component
public class VacationGrantRuleFinder {

	public VacationGrantRule find(String type) {
		VacationCode vacationCode = VacationCode.findByCode(type).orElseThrow();
		return find(vacationCode);
	}

	public VacationGrantRule find(VacationCode type) {
		switch (type) {
			case VacationCode.ANNUAL -> {
				return AnnualVacationGrantRule.statutory();
			}
			default -> {
				return new DefaultRule(type);
			}
		}
	}

	public VacationGrantRules findAll() {
		List<VacationGrantRule> rules = new ArrayList<>();
		for (VacationCode value : VacationCode.values()) {
			rules.add(find(value));
		}
		return new VacationGrantRules(rules);
	}

	public static class DefaultRule implements VacationGrantRule {

		private static final Integer DEFAULT_GRANT_DAYS = 0;
		private static final Integer DEFAULT_INITSERVICE_YEARS = 1;

		private final VacationCode type;

		public DefaultRule(VacationCode type) {
			this.type = type;
		}

		@Override
		public boolean canUpdate(double totalCount) {
			return true;
		}

		@Override
		public VacationInfo createVacationInfo(Long memberId) {
			return new VacationInfo(0, type.getCode(), memberId);
		}

		@Override
		public boolean isSameType(VacationCode vacationCode) {
			return this.type == vacationCode;
		}

		@Override
		public List<LocalDate> getBaseLineDates(LocalDate date) {
			return List.of(date.minusYears(DEFAULT_INITSERVICE_YEARS));
		}

		@Override
		public String getTypeCode() {
			return type.getCode();
		}

		@Override
		public VacationInfoLog grant(LocalDate date, Member member, VacationInfo info) {
			return info.init(DEFAULT_GRANT_DAYS);
		}
	}
}
