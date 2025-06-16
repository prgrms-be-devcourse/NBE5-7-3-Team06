package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.mock.VacationGrantRuleFake;
import programmers.team6.mock.VacationGrantRuleFinderFake;
import programmers.team6.mock.VacationInfoLogPublisherFake;
import programmers.team6.support.MemberMother;
import programmers.team6.support.TestVacationInfoBuilder;

class VacationGrantServiceTest {

	@Test
	@DisplayName("휴가_정상적으로_부여")
	void grants_vacation_successfully() {
		VacationInfo annualInfo = new TestVacationInfoBuilder()
			.memberId(1L)
			.totalCount(15)
			.vacationType(VacationCode.ANNUAL.getCode()).build();
		VacationInfo rewardInfo = new TestVacationInfoBuilder()
			.memberId(1L)
			.totalCount(15)
			.vacationType(VacationCode.REWARD.getCode()).build();
		VacationInfoRepository vacationInfoRepository = Mockito.mock(VacationInfoRepository.class);
		when(vacationInfoRepository.findAnnualVacationByJoinDates(any(), anyList())).thenReturn(
			List.of());
		when(vacationInfoRepository.findAnnualVacationByJoinDates(eq(VacationCode.ANNUAL.getCode()),
			anyList())).thenReturn(
			List.of(annualInfo));
		when(vacationInfoRepository.findByTypeAndCreatedAtToDate(eq(VacationCode.REWARD.getCode()),
			anyList())).thenReturn(
			List.of(rewardInfo));

		MemberRepository memberRepository = mock(MemberRepository.class);
		Member member = MemberMother.withId(1L);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		VacationInfoLogPublisherFake vacationInfoLogPublisherFake = new VacationInfoLogPublisherFake();
		VacationGrantRuleFinder vacationGrantRuleFinder = new VacationGrantRuleFinderFake(
			new VacationGrantRuleFake(VacationCode.ANNUAL),
			new VacationGrantRuleFake(VacationCode.REWARD));

		VacationGrantService vacationGrantService = new VacationGrantService(memberRepository, vacationInfoRepository,
			vacationGrantRuleFinder
			, vacationInfoLogPublisherFake);

		vacationGrantService.grantAnnualVacations(member.getJoinDate().toLocalDate().plusYears(1));

		assertThat(annualInfo.getTotalCount()).isEqualTo(10);
		assertThat(annualInfo.getVacationType()).isEqualTo(VacationCode.ANNUAL.getCode());
		assertThat(annualInfo.getUseCount()).isZero();
		assertThat(annualInfo.getMemberId()).isEqualTo(1L);

		assertThat(rewardInfo.getTotalCount()).isEqualTo(10);
		assertThat(rewardInfo.getVacationType()).isEqualTo(VacationCode.REWARD.getCode());
		assertThat(rewardInfo.getUseCount()).isZero();
		assertThat(rewardInfo.getMemberId()).isEqualTo(1L);
		assertThat(vacationInfoLogPublisherFake.isSameInput(List.of(
			new VacationInfoLog(10.0, 0, VacationCode.ANNUAL.getCode(), 1L),
			new VacationInfoLog(10.0, 0, VacationCode.REWARD.getCode(), 1L)
		))).isTrue();
	}
}