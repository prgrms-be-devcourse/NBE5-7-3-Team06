package programmers.team6.mock

import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.rule.VacationGrantRule
import programmers.team6.support.TestVacationInfoBuilder
import java.time.LocalDate

class VacationGrantRuleFake(private val vacationCode: VacationCode) : VacationGrantRule {
    override fun canUpdate(totalCount: Double): Boolean {
        return true
    }

    override fun createVacationInfo(memberId: Long): VacationInfo {
        return TestVacationInfoBuilder()
            .memberId(memberId)
            .totalCount(15.0)
            .vacationType(vacationCode.code).build()
    }

    override fun isSameType(vacationCode: VacationCode?): Boolean {
        return vacationCode == this.vacationCode
    }

    override fun getBaseLineDates(date: LocalDate): List<LocalDate> {
        return java.util.List.of(date)
    }

    override val typeCode: String
        get() = vacationCode.code

    override fun grant(date: LocalDate, member: Member, info: VacationInfo): VacationInfoLog {
        return info.init(10.0)
    }
}
