package programmers.team6.domain.vacation.rule;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.global.entity.Positive;

class MonthlyVacationRuleTest {

	@Test
	void 지급정보생성() {
		int grantDays = 2;
		MonthlyVacationRule monthlyVacationRule = new MonthlyVacationRule(new Positive(grantDays));
		VacationInfo info = createTestVacationInfo(15);

		VacationInfoLog log = monthlyVacationRule.grant(info);

		assertThat(info.getTotalCount()).isEqualTo(17.0);
		assertThat(log.getTotalCount()).isEqualTo(17.0);
	}

	private VacationInfo createTestVacationInfo(int totalCount) {
		return new VacationInfo(totalCount, 0, "01", 1L);
	}
}