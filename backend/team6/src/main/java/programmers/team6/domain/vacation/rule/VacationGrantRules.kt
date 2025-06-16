package programmers.team6.domain.vacation.rule

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.support.VacationInfos
import java.time.LocalDate

class VacationGrantRules(rules: List<VacationGrantRule>) {
    private val rules: Map<String, VacationGrantRule> = rules.associateBy { it.typeCode }

    fun getRules(): List<VacationGrantRule> = rules.values.toList()

    fun grant(date: LocalDate, member: Member, vacationInfos: VacationInfos): List<VacationInfoLog> {
        return vacationInfos.all.mapNotNull { info ->
            rules[info.vacationType]?.grant(date, member, info)
        }
    }
}
