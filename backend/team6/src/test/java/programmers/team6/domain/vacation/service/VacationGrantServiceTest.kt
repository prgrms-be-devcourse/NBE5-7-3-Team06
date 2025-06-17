package programmers.team6.domain.vacation.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.mock.VacationGrantRuleFake
import programmers.team6.mock.VacationGrantRuleFinderFake
import programmers.team6.mock.VacationInfoLogPublisherFake
import programmers.team6.support.MemberMother
import java.util.List

internal class VacationGrantServiceTest {
    @Test
    @DisplayName("휴가_정상적으로_부여")
    fun grants_vacation_successfully() {

        val annualInfo = VacationInfo(
            memberId = 1L,
            totalCount = 15.0,
            vacationType = VacationCode.ANNUAL.code
        )
        val rewardInfo = VacationInfo(
            memberId = 1L,
            totalCount = 15.0,
            vacationType = VacationCode.REWARD.code
        )
        val vacationInfoRepository = mockk<VacationInfoRepository>()

        every {
            vacationInfoRepository.findAnnualVacationByJoinDates(
                any(),
                any()
            )
        } returns listOf()

        every {
            vacationInfoRepository.findAnnualVacationByJoinDates(
                VacationCode.ANNUAL.code,
                any()
            )
        } returns listOf(annualInfo)

        every {
            vacationInfoRepository.findByTypeAndCreatedAtToDate(
                VacationCode.REWARD.code,
                any()
            )
        } returns listOf(rewardInfo)

        val memberRepository = mockk<MemberRepository>()
        val member = MemberMother.withId(1L)
        every { memberRepository.findByIdOrNull(1L) } returns member

        val vacationInfoLogPublisherFake = VacationInfoLogPublisherFake()
        val vacationGrantRuleFinder: VacationGrantRuleFinder = VacationGrantRuleFinderFake(
            VacationGrantRuleFake(VacationCode.ANNUAL),
            VacationGrantRuleFake(VacationCode.REWARD)
        )

        val vacationGrantService = VacationGrantService(
            memberRepository, vacationInfoRepository,
            vacationGrantRuleFinder,
            vacationInfoLogPublisherFake
        )

        vacationGrantService.grantAnnualVacations(member.joinDate.toLocalDate().plusYears(1))

        Assertions.assertThat(annualInfo.totalCount).isEqualTo(10.0)
        Assertions.assertThat(annualInfo.vacationType).isEqualTo(VacationCode.ANNUAL.code)
        Assertions.assertThat(annualInfo.useCount).isZero()
        Assertions.assertThat(annualInfo.memberId).isEqualTo(1L)

        Assertions.assertThat(rewardInfo.totalCount).isEqualTo(10.0)
        Assertions.assertThat(rewardInfo.vacationType).isEqualTo(VacationCode.REWARD.code)
        Assertions.assertThat(rewardInfo.useCount).isZero()
        Assertions.assertThat(rewardInfo.memberId).isEqualTo(1L)
        Assertions.assertThat(
            vacationInfoLogPublisherFake.isSameInput(
                List.of(
                    VacationInfoLog(10.0, 0.0, VacationCode.ANNUAL.code, 1L),
                    VacationInfoLog(10.0, 0.0, VacationCode.REWARD.code, 1L)
                )
            )
        ).isTrue()
    }
}