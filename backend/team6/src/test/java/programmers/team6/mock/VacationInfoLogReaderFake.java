package programmers.team6.mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.admin.support.VacationInfoLogReader;
import programmers.team6.domain.admin.support.VacationInfoLogs;
import programmers.team6.domain.vacation.entity.VacationInfoLog;

public class VacationInfoLogReaderFake extends VacationInfoLogReader {

	private final Map<Long, VacationInfoLog> vacationInfoLogs;

	public VacationInfoLogReaderFake(VacationInfoLog... logs) {
		super(null);
		this.vacationInfoLogs = toMap(Arrays.asList(logs));
	}

	private static Map<Long, VacationInfoLog> toMap(List<VacationInfoLog> vacationRequests) {
		Map<Long, VacationInfoLog> map = new HashMap<>();
		for (VacationInfoLog infoLog : vacationRequests) {
			map.put(infoLog.getMemberId(), infoLog);
		}
		return map;
	}

	@Override
	public VacationInfoLogs lastedLogsFrom(List<Long> ids, VacationStatisticsRequest request) {
		return new VacationInfoLogs(vacationInfoLogs);
	}

}
