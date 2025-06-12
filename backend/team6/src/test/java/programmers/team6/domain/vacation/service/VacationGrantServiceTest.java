package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.rule.VacationGrantRules;
import programmers.team6.support.MemberMother;

class VacationGrantServiceTest {

	@Test
	void 휴가_정상적으로_부여() {
		VacationInfo annualInfo = new VacationInfoTestBuilder()
			.memberId(1L)
			.totalCount(15)
			.vacationType(VacationCode.ANNUAL.getCode()).build();
		VacationInfo rewardInfo = new VacationInfoTestBuilder()
			.memberId(1L)
			.totalCount(15)
			.vacationType(VacationCode.REWARD.getCode()).build();
		VacationInfoRepository vacationInfoRepository = Mockito.mock(VacationInfoRepository.class);
		when(vacationInfoRepository.findAnnualVacationByJoinDates(any(), anyList())).thenReturn(
			List.of());
		when(vacationInfoRepository.findAnnualVacationByJoinDates(eq(VacationCode.ANNUAL.getCode()), anyList())).thenReturn(
			List.of(annualInfo));
		when(vacationInfoRepository.findByTypeAndCreatedAtToDate(eq(VacationCode.REWARD.getCode()), anyList())).thenReturn(
			List.of(rewardInfo));

		MemberRepository memberRepository = mock(MemberRepository.class);
		Member member = MemberMother.withId(1L);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		VacationInfoLogPublisherFake vacationInfoLogPublisherFake = new VacationInfoLogPublisherFake();
		VacationGrantService vacationGrantService = new VacationGrantService(memberRepository, vacationInfoRepository,
			new VacationGrantRuleFinderFake(), vacationInfoLogPublisherFake);

		vacationGrantService.grantAnnualVacations(member.getJoinDate().toLocalDate().plusYears(1));

		assertThat(annualInfo.getTotalCount()).isEqualTo(10);
		assertThat(annualInfo.getVacationType()).isEqualTo(VacationCode.ANNUAL.getCode());
		assertThat(annualInfo.getUseCount()).isZero();
		assertThat(annualInfo.getMemberId()).isEqualTo(1L);

		assertThat(rewardInfo.getTotalCount()).isEqualTo(10);
		assertThat(rewardInfo.getVacationType()).isEqualTo(VacationCode.REWARD.getCode());
		assertThat(rewardInfo.getUseCount()).isZero();
		assertThat(rewardInfo.getMemberId()).isEqualTo(1L);
		vacationInfoLogPublisherFake.isSameInput(List.of(
			new VacationInfoLog(10.0,0,VacationCode.ANNUAL.getCode(),1L),
			new VacationInfoLog(10.0,0,VacationCode.REWARD.getCode(),1L)
		));
	}

	private static class VacationInfoTestBuilder {
		private double totalCount;
		private String vacationType;
		private Long memberId;

		public VacationInfoTestBuilder totalCount(double totalCount) {
			this.totalCount = totalCount;
			return this;
		}

		public VacationInfoTestBuilder vacationType(String vacationType) {
			this.vacationType = vacationType;
			return this;
		}

		public VacationInfoTestBuilder memberId(Long memberId) {
			this.memberId = memberId;
			return this;
		}

		public VacationInfo build() {
			return new VacationInfo(totalCount, vacationType, memberId);
		}
	}

	private static class VacationGrantRuleFinderFake extends VacationGrantRuleFinder {

		@Override
		public VacationGrantRules findAll() {
			return new VacationGrantRules(List.of(new VacationGrantRuleFake(VacationCode.ANNUAL),
				new VacationGrantRuleFake(VacationCode.REWARD)));
		}
	}

	private static class VacationInfoLogPublisherFake extends VacationInfoLogPublisher {

		private List<VacationInfoLog> vacationInfoLogs = new ArrayList<>();

		public VacationInfoLogPublisherFake() {
			super(null);
		}

		@Override
		public void publish(List<VacationInfoLog> logs) {
			vacationInfoLogs.addAll(logs);
		}

		public boolean isSameInput(List<VacationInfoLog> logs) {
			for (VacationInfoLog log : logs) {
				if (!contains(log)) {
					return false;
				}
			}
			return true;
		}

		private boolean contains(VacationInfoLog log) {
			for (VacationInfoLog vacationInfoLog : vacationInfoLogs) {
				if (isSameInput(vacationInfoLog, log)) {
					return true;
				}
			}
			return false;
		}

		private boolean isSameInput(VacationInfoLog log1, VacationInfoLog log2) {
			if (!log1.getMemberId().equals(log2.getMemberId())) {
				return false;
			}
			if (!log1.getVacationType().equals(log2.getVacationType())) {
				return false;
			}
			if (log1.getTotalCount() != log2.getTotalCount()) {
				return false;
			}
			if (log1.getUseCount() != log2.getUseCount()) {
				return false;
			}
			return true;
		}
	}

	private static class VacationGrantRuleFake implements VacationGrantRule {

		private final VacationCode vacationCode;

		public VacationGrantRuleFake(VacationCode vacationCode) {
			this.vacationCode = vacationCode;
		}

		@Override
		public boolean canUpdate(double totalCount) {
			return true;
		}

		@Override
		public VacationInfo createVacationInfo(Long memberId) {
			return new VacationInfoTestBuilder()
				.memberId(memberId)
				.totalCount(15)
				.vacationType(vacationCode.getCode()).build();
		}

		@Override
		public boolean isSameType(VacationCode vacationCode) {
			return vacationCode == this.vacationCode;
		}

		@Override
		public List<LocalDate> getBaseLineDates(LocalDate date) {
			return List.of(date);
		}

		@Override
		public String getTypeCode() {
			return vacationCode.getCode();
		}

		@Override
		public VacationInfoLog grant(LocalDate date, Member member, VacationInfo info) {
			return info.init(10L);
		}
	}
}