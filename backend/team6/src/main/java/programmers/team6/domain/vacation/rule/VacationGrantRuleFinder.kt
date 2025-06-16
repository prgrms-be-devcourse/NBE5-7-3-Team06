package programmers.team6.domain.vacation.rule

import org.springframework.stereotype.Component
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import java.time.LocalDate


private const val DEFAULT_GRANT_DAYS = 0
private const val DEFAULT_INIT_SERVICE_YEARS = 1

@Component
open class VacationGrantRuleFinder {

    fun find(type: String): VacationGrantRule {
        val vacationCode = VacationCode.findByCode(type).orElseThrow()
        return find(vacationCode)
    }

    fun find(type: VacationCode): VacationGrantRule =
        when (type) {
            VacationCode.ANNUAL -> AnnualVacationGrantRule.statutory()
            else -> DefaultRule(type)
        }

    fun findAll(): VacationGrantRules =
        VacationGrantRules(VacationCode.entries.map(::find))

    class DefaultRule(private val type: VacationCode) : VacationGrantRule {

        override fun canUpdate(totalCount: Double) = true

        override fun createVacationInfo(memberId: Long): VacationInfo =
            VacationInfo(0.0, type.code, memberId)

        override fun isSameType(vacationCode: VacationCode?): Boolean =
            this.type == vacationCode

        override fun getBaseLineDates(date: LocalDate): List<LocalDate> =
            listOf(date.minusYears(DEFAULT_INIT_SERVICE_YEARS.toLong()))

        override val typeCode: String
            get() = type.code

        override fun grant(date: LocalDate, member: Member, info: VacationInfo): VacationInfoLog =
            info.init(DEFAULT_GRANT_DAYS.toDouble())

    }
}
