package programmers.team6.global.util;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DateUtilTest {

	@ParameterizedTest
	@CsvSource({"2024-10-18,true", "2024-10-19,true", "2024-10-17,false"})
	void isEqualsOrBeforeTest(LocalDate input, Boolean expected) {
		LocalDate date = LocalDate.of(2024, 10, 18);
		assertThat(DateUtil.isEqualsOrBefore(date, input)).isEqualTo(expected);
	}

	@Test
	void 근무일계산테스트() {
		LocalDate date1 = LocalDate.of(2024, 10, 18);
		LocalDate date2 = LocalDate.of(2026, 10, 17);
		assertThat(DateUtil.calcYearsOfService(date1, date2)).isEqualTo(1);
	}
}