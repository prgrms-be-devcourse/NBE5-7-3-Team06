package programmers.team6.domain.vacation.rule;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import programmers.team6.global.entity.Positive;

class AnnualVacationRuleTest {

	@Test
	void 연차변환기준입사일_생성() {
		int boundaryYear = 2;
		AnnualVacationRule annualVacationRule = createVacationInfo(boundaryYear, 0, 0, 0);
		LocalDate date = LocalDate.of(2024, 10, 18);

		LocalDate result = annualVacationRule.getJoinDate(date);

		assertThat(result).isEqualTo(LocalDate.of(2022, 10, 18));
	}

	private AnnualVacationRule createVacationInfo(Integer boundaryYear, Integer increaseYear,
		Integer vacationIncreaseDays, Integer initialGrantDays) {
		return new AnnualVacationRule(new Positive(boundaryYear), new Positive(increaseYear),
			new Positive(vacationIncreaseDays), new Positive(initialGrantDays));
	}
}