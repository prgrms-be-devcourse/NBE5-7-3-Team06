package programmers.team6.mock

import programmers.team6.domain.vacation.rule.VacationGrantRule
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.rule.VacationGrantRules
import java.util.*

class VacationGrantRuleFinderFake(vararg rules: VacationGrantRule) : VacationGrantRuleFinder() {
    private val rules: List<VacationGrantRule> =
        ArrayList(Arrays.asList(*rules))

    override fun findAll(): VacationGrantRules {
        return VacationGrantRules(rules)
    }
}
