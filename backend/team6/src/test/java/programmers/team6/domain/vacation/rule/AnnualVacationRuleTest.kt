package programmers.team6.domain.vacation.rule

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import programmers.team6.global.entity.Positive
import java.time.LocalDate

internal class AnnualVacationRuleTest {
    @Test
    fun 연차변환기준입사일_생성() {
        val boundaryYear = 2
        val annualVacationRule = createVacationInfo(boundaryYear, 0, 0, 0)
        val date = LocalDate.of(2024, 10, 18)

        val result = annualVacationRule.getJoinDate(date)

        Assertions.assertThat(result).isEqualTo(LocalDate.of(2022, 10, 18))
    }

    private fun createVacationInfo(
        boundaryYear: Int, increaseYear: Int,
        vacationIncreaseDays: Int, initialGrantDays: Int
    ): AnnualVacationRule {
        return AnnualVacationRule(
            Positive(boundaryYear), Positive(increaseYear),
            Positive(vacationIncreaseDays), Positive(initialGrantDays)
        )
    }
}