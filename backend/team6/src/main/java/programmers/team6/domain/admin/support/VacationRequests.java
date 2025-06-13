package programmers.team6.domain.admin.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import programmers.team6.domain.vacation.entity.VacationRequest;

public class VacationRequests {

	private final Map<Long, List<VacationRequest>> requests;

	public VacationRequests(List<VacationRequest> requests) {
		this.requests = toMap(requests);
	}

	private Map<Long, List<VacationRequest>> toMap(List<VacationRequest> requests) {
		return requests.stream().collect(Collectors.groupingBy(VacationRequest::getMemberId));
	}

	public TargetVacationRequests targetRequests(Long memberId) {
		return new TargetVacationRequests(findByMemberId(memberId));
	}

	private List<VacationRequest> findByMemberId(Long memberId) {
		return requests.getOrDefault(memberId, Collections.emptyList());
	}
}
