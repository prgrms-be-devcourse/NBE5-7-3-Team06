package programmers.team6.domain.admin.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.dto.VacationMonthlyStatisticsResponse;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.mock.MemberReaderFake;
import programmers.team6.mock.VacationInfoLogReaderFake;
import programmers.team6.mock.VacationRequestsReaderFake;
import programmers.team6.support.MemberMother;
import programmers.team6.support.TestVacationType;

class VacationStatisticsServiceTest {

	@Test
	@DisplayName("사용자의 휴가정보를 출력한다")
	void display_user_vacation_info() {
		MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
		Member member1 = MemberMother.withId(1L);
		Member member2 = MemberMother.withId(2L);
		MemberReader memberReader = new MemberReaderFake(member1, member2);

		VacationInfoLog log1 = new VacationInfoLog(13, 0, "01", 1L);
		VacationInfoLog log2 = new VacationInfoLog(13, 0, "01", 2L);
		VacationInfoLogReaderFake vacationInfoLogReaderFake = new VacationInfoLogReaderFake(log1, log2);

		VacationRequest vacationRequest1 = VacationRequest.builder()
			.from(LocalDateTime.of(2024, 5, 13, 0, 0))
			.to(LocalDateTime.of(2024, 5, 14, 0, 0))
			.member(member1)
			.type(TestVacationType.ANNUAL.toCode()).build();
		VacationRequest vacationRequest2 = VacationRequest.builder()
			.from(LocalDateTime.of(2024, 5, 13, 0, 0))
			.to(LocalDateTime.of(2024, 5, 14, 0, 0))
			.member(member2)
			.type(TestVacationType.ANNUAL.toCode()).build();
		VacationRequestsReader vacationRequestsReaderFake = new VacationRequestsReaderFake(
			vacationRequest1
			, vacationRequest2);

		VacationStatisticsService vacationStatisticsService = new VacationStatisticsService(memberRepository,
			vacationInfoLogReaderFake, vacationRequestsReaderFake, memberReader, new VacationStatisticsMapper());
		VacationStatisticsRequest vacationStatisticsRequest = new VacationStatisticsRequest(2024, null, null, "01");
		PageRequest pageRequest = PageRequest.of(0, 10);

		Page<VacationMonthlyStatisticsResponse> statistics = vacationStatisticsService.getMonthlyVacationStatistics(
			vacationStatisticsRequest, pageRequest);

		assertThat(statistics).hasSize(2);
		List<VacationMonthlyStatisticsResponse> response = List.of(
			createVacationMonthlyStatisticsResponse(member1, log1),
			createVacationMonthlyStatisticsResponse(member2, log2));
		assertThat(statistics).isEqualTo(new PageImpl<>(response, pageRequest, statistics.getTotalElements()));
	}

	private VacationMonthlyStatisticsResponse createVacationMonthlyStatisticsResponse(Member member,
		VacationInfoLog vacationInfoLog) {
		return new VacationMonthlyStatisticsResponse(
			member.getId(),
			member.getName(),
			vacationInfoLog.getTotalCount(),
			vacationInfoLog.getUseCount(),
			vacationInfoLog.getTotalCount() - vacationInfoLog.getUseCount(),
			0,
			0,
			0,
			0,
			2.0,
			0,
			0,
			0,
			0,
			0,
			0,
			0
		);
	}
}