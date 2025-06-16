package programmers.team6.domain.admin.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.support.TargetVacationRequests
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.support.MemberMother
import programmers.team6.support.TestVacationType
import java.time.LocalDateTime

internal class TargetVacationRequestsTest {
    @Test
    @DisplayName("지정된 월의 사용된 휴가 개수를 센다")
    fun counts_used_vacation_in_given_month() {
        val vacationRequest: VacationRequest = VacationRequest(
            MemberMother.withId(0L),
            LocalDateTime.of(2024, 12, 31, 0, 0),
            LocalDateTime.of(2025, 1, 1, 0, 0),
            "",
            TestVacationType.ANNUAL.toCode()
        )

        val targetVacationRequests = TargetVacationRequests(listOf(vacationRequest))

        val count = targetVacationRequests.count(2024, 12)

        Assertions.assertThat(count).isEqualTo(1.0)
    }
}