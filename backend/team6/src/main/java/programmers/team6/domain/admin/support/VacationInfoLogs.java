package programmers.team6.domain.admin.support;

import java.util.Map;

import programmers.team6.domain.vacation.entity.VacationInfoLog;

public class VacationInfoLogs {

	private final Map<Long, VacationInfoLog> lastedMap;

	public VacationInfoLogs(Map<Long, VacationInfoLog> lastedMap) {
		this.lastedMap = lastedMap;
	}

	public VacationInfoLog findVacationInfo(Long id) {
		return lastedMap.get(id);
	}
}
