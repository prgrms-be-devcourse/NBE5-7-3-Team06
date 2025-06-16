package programmers.team6.domain.vacation.rule

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import java.time.LocalDate

interface VacationGrantRule {
    fun canUpdate(totalCount: Double): Boolean

    fun createVacationInfo(memberId: Long): VacationInfo

    fun isSameType(vacationCode: VacationCode?): Boolean

    fun getBaseLineDates(date: LocalDate): List<LocalDate>

	val typeCode: String

    fun grant(date: LocalDate, member: Member, info: VacationInfo): VacationInfoLog
}
