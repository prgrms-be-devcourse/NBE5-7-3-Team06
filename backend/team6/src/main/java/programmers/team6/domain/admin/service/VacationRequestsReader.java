package programmers.team6.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;

@Component
@RequiredArgsConstructor
public class VacationRequestsReader {

	private final VacationRequestRepository vacationRequestRepository;

	public VacationRequests vacationRequestFrom(List<Long> ids, VacationStatisticsRequest request) {
		return new VacationRequests(
			vacationRequestRepository.findByMemberIdInAndYear(ids, request.year(), codes(request.vacationCode())));
	}

	private List<String> codes(String code) {
		if (code.equals("01")) {
			return List.of("01", "05");
		}
		return List.of(code);
	}
}
