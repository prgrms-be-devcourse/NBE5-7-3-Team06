package programmers.team6.domain.vacation.entity

import org.assertj.core.api.AssertionsForClassTypes.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class VacationInfoLogTests {
    @Test
    @DisplayName("남은 휴가 일수를 반환한다.")
    @Throws(Exception::class)
    fun remaining_count_test() {
        val total = 15
        val use = 3
        val vacationInfoLog = VacationInfoLog(total.toDouble(), use.toDouble(), "01", 1L)

        val remainCount = vacationInfoLog.remainingCount()

        assertThat(remainCount).isEqualTo((total - use).toDouble())
    }
}