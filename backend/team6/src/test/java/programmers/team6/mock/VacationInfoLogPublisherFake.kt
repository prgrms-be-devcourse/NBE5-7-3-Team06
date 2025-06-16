package programmers.team6.mock;

import java.util.ArrayList;
import java.util.List;

import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;

public class VacationInfoLogPublisherFake  extends VacationInfoLogPublisher {

	private List<VacationInfoLog> vacationInfoLogs = new ArrayList<>();

	public VacationInfoLogPublisherFake() {
		super(null);
	}

	@Override
	public void publish(List<VacationInfoLog> logs) {
		vacationInfoLogs.addAll(logs);
	}

	public boolean isSameInput(List<VacationInfoLog> logs) {
		for (VacationInfoLog log : logs) {
			if (!contains(log)) {
				return false;
			}
		}
		return true;
	}

	private boolean contains(VacationInfoLog log) {
		for (VacationInfoLog vacationInfoLog : vacationInfoLogs) {
			if (isSameInput(vacationInfoLog, log)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSameInput(VacationInfoLog log1, VacationInfoLog log2) {
		if (!log1.getMemberId().equals(log2.getMemberId())) {
			return false;
		}
		if (!log1.getVacationType().equals(log2.getVacationType())) {
			return false;
		}
		if (log1.getTotalCount() != log2.getTotalCount()) {
			return false;
		}
		if (log1.getUseCount() != log2.getUseCount()) {
			return false;
		}
		return true;
	}
}
