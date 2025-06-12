package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import programmers.team6.domain.admin.dto.VacationStatisticsRequest;
import programmers.team6.domain.admin.service.VacationInfoLogReader;
import programmers.team6.domain.admin.service.VacationInfoLogs;
import programmers.team6.domain.vacation.entity.VacationInfoLog;

public class VacationInfoLogReaderFake extends VacationInfoLogReader {

	private final Map<Long, List<VacationInfoLog>> vacationInfoLogs;

		public VacationInfoLogReaderFake(VacationInfoLog... logs) {
		super(null);
		this.vacationInfoLogs = toMap(Arrays.asList(logs));
	}

	private static Map<Long, List<VacationInfoLog>> toMap(List<VacationInfoLog> vacationRequests) {
		Map<Long, List<VacationInfoLog>> map = new HashMap<>();
		for (VacationInfoLog infoLog : vacationRequests) {
			if (!map.containsKey(infoLog.getMemberId())) {
				map.put(infoLog.getMemberId(), new ArrayList<>());
			}
			List<VacationInfoLog> logs = map.get(infoLog.getMemberId());
			logs.add(infoLog);
			logs.sort((log1, log2) -> log1.getLogDate().isAfter(log2.getLogDate()) ? -1 : 1);
		}
		return map;
	}

	@Override
	public VacationInfoLogs lastedLogsFrom(List<Long> ids, VacationStatisticsRequest request) {
		Map<Long, VacationInfoLog> result = new HashMap<>();
		for (Long id : ids) {
			if (vacationInfoLogs.containsKey(id)) {
				VacationInfoLog target = findTarget(vacationInfoLogs.get(id), request.vacationCode());
				if (target != null) {
					result.put(id, target);
				}

			}
		}
		return new VacationInfoLogs(result);
	}

	private VacationInfoLog findTarget(List<VacationInfoLog> logs, String code) {
		for (VacationInfoLog log : logs) {
			if (log.getVacationType().equals(code)) {
				return log;
			}
		}
		return null;
	}
}
