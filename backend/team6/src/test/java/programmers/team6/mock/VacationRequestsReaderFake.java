package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.admin.support.VacationRequests;
import programmers.team6.domain.admin.support.VacationRequestsReader;
import programmers.team6.domain.vacation.entity.VacationRequest;

public class VacationRequestsReaderFake extends VacationRequestsReader {

	private final List<VacationRequest> vacationRequests;

	public VacationRequestsReaderFake(VacationRequest... vacationRequests) {
		super(null);
		this.vacationRequests = new ArrayList<>(Arrays.asList(vacationRequests));
	}

	@Override
	public VacationRequests vacationRequestFrom(List<Long> ids, VacationStatisticsRequest request) {
		return new VacationRequests(vacationRequests);
	}
}
