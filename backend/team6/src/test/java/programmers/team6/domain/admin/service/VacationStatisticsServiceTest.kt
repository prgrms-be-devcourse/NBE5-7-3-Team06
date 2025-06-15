package programmers.team6.domain.admin.service

import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.support.MemberReader
import programmers.team6.domain.admin.support.VacationRequestsReader
import programmers.team6.domain.admin.utils.mapper.VacationStatisticsMapper
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.response.VacationMonthlyStatisticsResponse
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.mock.MemberReaderFake
import programmers.team6.mock.VacationInfoLogReaderFake
import programmers.team6.mock.VacationRequestsReaderFake
import programmers.team6.support.MemberMother
import programmers.team6.support.TestVacationType
import java.time.LocalDateTime
import java.util.List

internal class VacationStatisticsServiceTest {

    @Test
    @DisplayName("사용자의 휴가정보를 출력한다")
    fun display_user_vacation_info() {
        val memberRepository = mockk<MemberRepository>()
        val member1 = MemberMother.withId(1L)
        val member2 = MemberMother.withId(2L)
        val memberReader: MemberReader = MemberReaderFake(member1, member2)

        val log1 = VacationInfoLog(13.0, 0.0, "01", 1L)
        val log2 = VacationInfoLog(13.0, 0.0, "01", 2L)
        val vacationInfoLogReaderFake = VacationInfoLogReaderFake(log1, log2)

        val vacationRequest1 = VacationRequest.builder()
            .from(LocalDateTime.of(2024, 5, 13, 0, 0))
            .to(LocalDateTime.of(2024, 5, 14, 0, 0))
            .member(member1)
            .type(TestVacationType.ANNUAL.toCode()).build()
        val vacationRequest2 = VacationRequest.builder()
            .from(LocalDateTime.of(2024, 5, 13, 0, 0))
            .to(LocalDateTime.of(2024, 5, 14, 0, 0))
            .member(member2)
            .type(TestVacationType.ANNUAL.toCode()).build()
        val vacationRequestsReaderFake: VacationRequestsReader = VacationRequestsReaderFake(
            vacationRequest1,
            vacationRequest2
        )

        val vacationStatisticsRequest = VacationStatisticsRequest(2024, null, null, "01")
        val pageRequest = PageRequest.of(0, 10)

        val vacationStatisticsService = VacationStatisticsService(
            memberRepository,
            vacationInfoLogReaderFake,
            vacationRequestsReaderFake,
            memberReader,
            VacationStatisticsMapper()
        )

        val statistics = vacationStatisticsService.getMonthlyVacationStatistics(vacationStatisticsRequest, pageRequest)

        assertThat(statistics).hasSize(2)
        val response = listOf(
            createVacationMonthlyStatisticsResponse(member1, log1),
            createVacationMonthlyStatisticsResponse(member2, log2)
        )
        assertThat(statistics).isEqualTo(PageImpl(response, pageRequest, statistics.totalElements))
    }

    private fun createVacationMonthlyStatisticsResponse(
        member: Member,
        vacationInfoLog: VacationInfoLog
    ): VacationMonthlyStatisticsResponse =
        VacationMonthlyStatisticsResponse(
            member.id,
            member.name,
            vacationInfoLog.totalCount,
            vacationInfoLog.useCount,
            vacationInfoLog.totalCount - vacationInfoLog.useCount,
            0.0,
            0.0,
            0.0,
            0.0,
            2.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0
        )
}