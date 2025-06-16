package programmers.team6.domain.vacation.rule

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.global.entity.Positive
import java.time.LocalDate

private val STATUTORY_MAX_GRANT_DAYS = Positive(25)

class AnnualVacationGrantRule(
    private val annualVacationRule: AnnualVacationRule,
    private val monthlyVacationRule: MonthlyVacationRule,
    private val maxGrantDays: Positive
) : VacationGrantRule {

    override fun canUpdate(totalCount: Double): Boolean {
        return maxGrantDays.toInt() >= totalCount
    }

    override fun createVacationInfo(memberId: Long): VacationInfo {
        return annualVacationRule.vacationInfo(memberId)
    }

    override fun isSameType(vacationCode: VacationCode): Boolean {
        return annualVacationRule.isSameType(vacationCode)
    }

    override fun getBaseLineDates(date: LocalDate): List<LocalDate> {
        val result = ArrayList<LocalDate>()
        result.addAll(annualVacationRule.getBaseLineDates(date))
        result.addAll(monthlyVacationRule.getBaseLineDates(annualVacationRule.getJoinDate(date), date))
        return result
    }

    override val typeCode: String
        get() = annualVacationRule.type.code

    override fun grant(date: LocalDate, member: Member, info: VacationInfo): VacationInfoLog {
        if (annualVacationRule.isTarget(date, member)) {
            return grantAnnual(date, member, info)
        }
        return grantMonthly(info)
    }

    private fun grantAnnual(date: LocalDate, member: Member, vacationInfo: VacationInfo): VacationInfoLog {
        val log = annualVacationRule.grant(date, member, vacationInfo)
        if (maxGrantDays.toInt() < vacationInfo.totalCount) {
            return vacationInfo.updateTotalCount(maxGrantDays.toInt().toDouble())
        }
        return log
    }

    private fun grantMonthly(vacationInfo: VacationInfo): VacationInfoLog {
        val log = monthlyVacationRule.grant(vacationInfo)
        if (maxGrantDays.toInt() < vacationInfo.totalCount) {
            return vacationInfo.updateTotalCount(maxGrantDays.toInt().toDouble())
        }
        return log
    }

    companion object {

        fun statutory(): AnnualVacationGrantRule {
            return AnnualVacationGrantRule(
                AnnualVacationRule.statutory(), MonthlyVacationRule.statutory(),
                STATUTORY_MAX_GRANT_DAYS
            )
        }
    }
}
