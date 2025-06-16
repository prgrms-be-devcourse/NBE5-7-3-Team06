package programmers.team6.domain.vacation.rule

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.global.entity.Positive
import programmers.team6.global.util.DateUtil
import java.time.LocalDate


private val STATUTORY_BOUNDARY_YEAR = Positive(1)
private val STATUTORY_INCREASE_YEAR = Positive(2)
private val STATUTORY_INCREASE_DAYS = Positive(1)
private val STATUTORY_INITIAL_GRANT_DAYS = Positive(15)

class AnnualVacationRule(
    private val boundaryYear: Positive,
    private val increaseYear: Positive,
    private val vacationIncreaseDays: Positive,
    private val initialGrantDays: Positive
) {

    val type: VacationCode = VacationCode.ANNUAL

    fun getJoinDate(date: LocalDate): LocalDate = date.minusYears(boundaryYear.toInt().toLong())

    fun grant(date: LocalDate, member: Member, vacationInfo: VacationInfo): VacationInfoLog {
        val yearsOfService = DateUtil.calcYearsOfService(date, member.joinDate.toLocalDate())
        return vacationInfo.init(calcIncreaseDays(yearsOfService).toDouble())
    }

    fun vacationInfo(memberId: Long): VacationInfo =
        VacationInfo(initialGrantDays.toInt().toDouble(), type.code, memberId)

    fun getBaseLineDates(date: LocalDate): List<LocalDate> = listOf(date.minusYears(boundaryYear.toInt().toLong()))

    fun isSameType(vacationCode: VacationCode): Boolean = type == vacationCode

    fun isTarget(date: LocalDate, member: Member): Boolean {
        val joinDate = getJoinDate(date)
        return joinDate.isBefore(member.joinDate.toLocalDate()) || joinDate.isEqual(
            member.joinDate.toLocalDate()
        )
    }

    private fun calcIncreaseDays(yearsOfService: Int): Int =
        initialGrantDays.toInt() + calculateAdditionalVacationDays(yearsOfService)

    private fun calculateAdditionalVacationDays(yearsOfService: Int): Int =
        (((yearsOfService - boundaryYear.toInt()) / increaseYear.toInt()) * vacationIncreaseDays.toInt())

    companion object {

        fun statutory(): AnnualVacationRule {
            return AnnualVacationRule(
                STATUTORY_BOUNDARY_YEAR, STATUTORY_INCREASE_YEAR, STATUTORY_INCREASE_DAYS,
                STATUTORY_INITIAL_GRANT_DAYS
            )
        }
    }
}
