package programmers.team6.domain.vacation.rule;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.support.VacationInfos;

public class VacationGrantRules {
	private final Map<String, VacationGrantRule> rules;

	public VacationGrantRules(List<VacationGrantRule> rules) {
		this.rules = toMap(rules);
	}

	public List<VacationGrantRule> getRules() {
		return rules.values().stream().toList();
	}

	public List<VacationInfoLog> grant(LocalDate date, Member member, VacationInfos vacationInfos) {
		List<VacationInfoLog> result = new ArrayList<>();
		for (VacationInfo info : vacationInfos.getAll()) {
			VacationGrantRule rule = findRule(info.getVacationType());
			result.add(rule.grant(date, member, info));
		}
		return result;
	}

	private static Map<String, VacationGrantRule> toMap(List<VacationGrantRule> rules) {
		return rules.stream().collect(Collectors.toMap(VacationGrantRule::getTypeCode, rule -> rule));
	}

	private VacationGrantRule findRule(String vacationType) {
		return rules.get(vacationType);
	}
}
