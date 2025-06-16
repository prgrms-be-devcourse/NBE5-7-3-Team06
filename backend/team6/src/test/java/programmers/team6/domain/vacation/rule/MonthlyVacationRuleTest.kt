package programmers.team6.domain.vacation.rule

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.global.entity.Positive

internal class MonthlyVacationRuleTest {
    @Test
    fun 지급정보생성() {
        val grantDays = 2
        val monthlyVacationRule = MonthlyVacationRule(Positive(grantDays))
        val info = createTestVacationInfo(15)

        val log = monthlyVacationRule.grant(info)

        Assertions.assertThat(info.totalCount).isEqualTo(17.0)
        Assertions.assertThat(log.totalCount).isEqualTo(17.0)
    }

    private fun createTestVacationInfo(totalCount: Int): VacationInfo {
        return VacationInfo(totalCount.toDouble(), 0.0, "01", 1L)
    }
}