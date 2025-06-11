package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.auth.service.AuthService;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.dto.VacationRequestCalendarResponse;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;

@Slf4j
@SpringBootTest
@Transactional
class VacationServiceTests {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	VacationService vacationRequestService;
	@Autowired
	AuthService authService;
	@Autowired
	private VacationRequestRepository vacationRequestRepository;
	@Autowired
	private CodeRepository codeRepository;

	@Test
	@DisplayName("캘린더 결과 테스트")
	void select_calendar_test() throws Exception {

		MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
			"홍길동",
			"hong@naver.com",
			1L,
			"01",
			LocalDateTime.now(),
			"1990-01-01",
			"password1234");

		authService.signUp(memberSignUpRequest);

		Member savedMember = memberRepository.findByEmail("hong@naver.com")
			.orElseThrow(() -> new RuntimeException("회원 저장 실패 "));

		Code vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", "01")
			.orElseThrow(() -> new RuntimeException("잘못된 휴가 유형입니다."));

		VacationRequest vacationRequest = new VacationRequest(savedMember, LocalDateTime.now(), LocalDateTime.now(),
			"사유", vacationType,
			VacationRequestStatus.APPROVED, 1);

		for (int i = 0; i < 5; i++) {
			vacationRequestRepository.save(vacationRequest);
		}

		List<VacationRequestCalendarResponse> vacationRequestCalendarResponses = vacationRequestService.selectVacationCalendar(
			"2025-05", 1L);

		log.info("approvedVacationsByMonth.size() = {}", vacationRequestCalendarResponses.size());

		assertThat(vacationRequestCalendarResponses.size()).isNotEqualTo(0);

		for (VacationRequestCalendarResponse vacationRequestCalendarResponse : vacationRequestCalendarResponses) {
			log.info("vacationRequestCalendarResponse = {}", vacationRequestCalendarResponse);
		}
	}

}