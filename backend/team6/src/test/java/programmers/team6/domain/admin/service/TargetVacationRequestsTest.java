package programmers.team6.domain.admin.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.support.TestVacationType;

class TargetVacationRequestsTest {

	@Test
	@DisplayName("지정된 월의 사용된 휴가 개수를 센다")
	void counts_used_vacation_in_given_month() {
		VacationRequest vacationRequest = VacationRequest.builder()
			.from(LocalDateTime.of(2024, 12, 31, 0, 0))
			.to(LocalDateTime.of(2025, 1, 1, 0, 0))
			.type(TestVacationType.ANNUAL.toCode()).build();
		TargetVacationRequests targetVacationRequests = new TargetVacationRequests(List.of(vacationRequest));

		double count = targetVacationRequests.count(2024, 12);

		assertThat(count).isEqualTo(1);
	}

}