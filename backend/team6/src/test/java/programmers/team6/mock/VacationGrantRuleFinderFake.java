package programmers.team6.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.rule.VacationGrantRules;

public class VacationGrantRuleFinderFake extends VacationGrantRuleFinder {

	private List<VacationGrantRule> rules;

	public VacationGrantRuleFinderFake(VacationGrantRule... rules) {
		this.rules = new ArrayList<>(Arrays.asList(rules));
	}

	@Override
	public VacationGrantRules findAll() {
		return new VacationGrantRules(rules);
	}
}
