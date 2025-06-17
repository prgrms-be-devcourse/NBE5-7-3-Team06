package programmers.team6.domain.vacation.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.mock.VacationGrantRuleFake
import programmers.team6.mock.VacationGrantRuleFinderFake
import programmers.team6.mock.VacationInfoLogPublisherFake
import programmers.team6.support.MemberMother
import programmers.team6.support.TestVacationInfoBuilder
import java.util.*
import java.util.List

internal class VacationGrantServiceTest {
    @Test
    @DisplayName("휴가_정상적으로_부여")
    fun grants_vacation_successfully() {
        val annualInfo = TestVacationInfoBuilder()
            .memberId(1L)
            .totalCount(15.0)
            .vacationType(VacationCode.ANNUAL.code).build()
        val rewardInfo = TestVacationInfoBuilder()
            .memberId(1L)
            .totalCount(15.0)
            .vacationType(VacationCode.REWARD.code).build()
        val vacationInfoRepository = Mockito.mock(VacationInfoRepository::class.java)
        Mockito.`when`(
            vacationInfoRepository.findAnnualVacationByJoinDates(
                ArgumentMatchers.any(),
                ArgumentMatchers.anyList()
            )
        ).thenReturn(
            listOf()
        )
        Mockito.`when`(
            vacationInfoRepository.findAnnualVacationByJoinDates(
                ArgumentMatchers.eq(VacationCode.ANNUAL.code),
                ArgumentMatchers.anyList()
            )
        ).thenReturn(
            List.of(annualInfo)
        )
        Mockito.`when`(
            vacationInfoRepository.findByTypeAndCreatedAtToDate(
                ArgumentMatchers.eq(VacationCode.REWARD.code),
                ArgumentMatchers.anyList()
            )
        ).thenReturn(
            List.of(rewardInfo)
        )

        val memberRepository = Mockito.mock(MemberRepository::class.java)
        val member = MemberMother.withId(1L)
        Mockito.`when`(memberRepository.findById(1L)).thenReturn(Optional.of(member))

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