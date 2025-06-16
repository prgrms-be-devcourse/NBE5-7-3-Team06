package programmers.team6.domain.admin.support

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.support.MemberMother
import programmers.team6.support.VacationTypeMother
import java.time.LocalDateTime
import java.util.List

internal class VacationRequestsReaderTest {
    @Test
    fun 연차휴가요청내역조회() {
        val repository = mockk<VacationRequestRepository>()
        val memberIds = listOf(1L)

        val annualVacationRequest = VacationRequest(
            from = LocalDateTime.of(2024, 1, 1, 1, 1),
            to = LocalDateTime.of(2024, 1, 1, 1, 1),
            member = MemberMother.withId(1L),
            type = VacationTypeMother.Annual(),
            status = VacationRequestStatus.APPROVED,
            reason = ""
        )

        val halfVacationRequest = VacationRequest(
            from = LocalDateTime.of(2024, 1, 1, 1, 1),
            to = LocalDateTime.of(2024, 1, 1, 1, 1),
            member = MemberMother.withId(1L),
            type = VacationTypeMother.half(),
            status = VacationRequestStatus.APPROVED,
            reason = ""
        )

        every { repository.findByMemberIdInAndYear(
            memberIds, 2024, listOf("01", "05")
        ) }.returns(listOf(annualVacationRequest, halfVacationRequest))

        val reader = VacationRequestsReader(repository)

        val vacationRequests = reader.vacationRequestFrom(
            memberIds, VacationStatisticsRequest(
                2024, null, null, "01"
            )
        )

        assertThat(vacationRequests)
            .isEqualTo(VacationRequests(listOf(annualVacationRequest, halfVacationRequest)))
    }

    @Test
    fun 일반휴가내역() {
        val repository = mockk<VacationRequestRepository>();
        val memberIds = listOf(1L)
        val rewardVacationRequest = VacationRequest(
            from = LocalDateTime.of(2024, 1, 1, 1, 1),
            to = LocalDateTime.of(2024, 1, 1, 1, 1),
            member = MemberMother.withId(1L),
            type = VacationTypeMother.reward(),
            status = VacationRequestStatus.APPROVED,
            reason = ""
        )

        every {  repository.findByMemberIdInAndYear(
            memberIds, 2024, listOf("02")
        ) }.returns(listOf(rewardVacationRequest))

        val reader = VacationRequestsReader(repository)

        val vacationRequests = reader.vacationRequestFrom(
            memberIds, VacationStatisticsRequest(
                2024, null, null, "02"
            )
        )

        assertThat(vacationRequests).isEqualTo(VacationRequests(listOf(rewardVacationRequest)))
    }
}