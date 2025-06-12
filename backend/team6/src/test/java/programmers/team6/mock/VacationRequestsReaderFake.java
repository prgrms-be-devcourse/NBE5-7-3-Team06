package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.admin.service.VacationRequests;
import programmers.team6.domain.admin.service.VacationRequestsReader;
import programmers.team6.domain.vacation.entity.VacationRequest;

public class VacationRequestsReaderFake extends VacationRequestsReader {

	private final List<VacationRequest> vacationRequests;

	public VacationRequestsReaderFake(VacationRequest... vacationRequests) {
		super(null);
		this.vacationRequests = new ArrayList<>(Arrays.asList(vacationRequests));
	}

	@Override
	public VacationRequests vacationRequestFrom(List<Long> ids, VacationStatisticsRequest request) {
		List<VacationRequest> list = vacationRequests
			.stream()
			.filter(vacationRequest -> ids.contains(vacationRequest.getMemberId()))
			.filter(vacationRequest -> vacationRequest.getFrom().getYear() == request.year()
				|| vacationRequest.getTo().getYear() == request.year())
			.toList();
		return new VacationRequests(list);
	}
}
