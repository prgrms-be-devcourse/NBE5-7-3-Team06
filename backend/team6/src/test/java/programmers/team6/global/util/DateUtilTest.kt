package programmers.team6.global.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate

internal class DateUtilTest {
    @ParameterizedTest
    @CsvSource("2024-10-18,true", "2024-10-19,true", "2024-10-17,false")
    fun isEqualsOrBeforeTest(input: LocalDate, expected: Boolean) {
        val date = LocalDate.of(2024, 10, 18)
        Assertions.assertThat(DateUtil.isEqualsOrBefore(date, input)).isEqualTo(expected)
    }

    @Test
    fun 근무일계산테스트() {
        val date1 = LocalDate.of(2024, 10, 18)
        val date2 = LocalDate.of(2026, 10, 17)
        Assertions.assertThat(DateUtil.calcYearsOfService(date1, date2)).isEqualTo(1)
    }
}