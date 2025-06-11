package programmers.team6.domain.vacation.rule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.constraints.NotNull;
import programmers.team6.domain.vacation.entity.VacationInfo;

public final class VacationInfos {
	private final Map<Long, List<VacationInfo>> infos;

	public VacationInfos(List<VacationInfo> infos) {
		this.infos = toMap(infos);
	}

	private static Map<Long, List<VacationInfo>> toMap(List<VacationInfo> infos) {
		return infos.stream().collect(Collectors.groupingBy(VacationInfo::getMemberId));
	}

	public List<Long> getMemberIds() {
		return infos.keySet().stream().toList();
	}

	public List<VacationInfo> getByMemberId(@NotNull Long id) {
		return infos.getOrDefault(id, Collections.emptyList());
	}

	public List<VacationInfo> getAll() {
		return infos.values().stream()
			.flatMap(List::stream)
			.toList();
	}
}
