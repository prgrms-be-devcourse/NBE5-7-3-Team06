package programmers.team6.domain.vacation.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VacationInfoLogTests {

	@Test
	@DisplayName("남은 휴가 일수를 반환한다.")
	void remaining_count_test() throws Exception {
		int total = 15;
		int use = 3;
		VacationInfoLog vacationInfoLog = new VacationInfoLog(total, use, "01", 1L);

		double remainCount = vacationInfoLog.remainingCount();

		assertThat(remainCount).isEqualTo(total - use);
	}

}