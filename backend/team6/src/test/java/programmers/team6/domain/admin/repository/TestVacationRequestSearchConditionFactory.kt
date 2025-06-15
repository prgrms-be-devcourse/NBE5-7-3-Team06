package programmers.team6.domain.admin.repository

import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.bindingApplicantCondition
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.bindingDateRangeCondition
import programmers.team6.domain.admin.enums.Quarter
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDate

object TestVacationRequestSearchConditionFactory {
    @JvmStatic
    fun createByDateRange(start: LocalDate?, end: LocalDate?): AdminVacationSearchCondition {
        return AdminVacationSearchCondition(
            bindingDateRangeCondition(start, end, null, null), AdminVacationSearchCondition.DEFAULT_APPLICANT, null
        )
    }

    @JvmStatic
    fun createByDateRange(year: Int?, quarter: Quarter?): AdminVacationSearchCondition {
        return AdminVacationSearchCondition(
            bindingDateRangeCondition(null, null, year, quarter), AdminVacationSearchCondition.DEFAULT_APPLICANT, null
        )
    }

    @JvmStatic
    fun createByApplicant(name: String?, deptName: String?): AdminVacationSearchCondition {
        return AdminVacationSearchCondition(
            AdminVacationSearchCondition.DEFAULT_DATE_RANGE,
            bindingApplicantCondition(name, deptName, null, null), null
        )
    }

    @JvmStatic
    fun createByApplicant(positionCodeId: Long?, vacationTypeCodeId: Long?): AdminVacationSearchCondition {
        return AdminVacationSearchCondition(
            AdminVacationSearchCondition.DEFAULT_DATE_RANGE,
            bindingApplicantCondition(null, null, positionCodeId, vacationTypeCodeId),
            null
        )
    }

    fun createByVacationRequestStatus(
        vacationRequestStatus: VacationRequestStatus?
    ): AdminVacationSearchCondition {
        return AdminVacationSearchCondition(
            AdminVacationSearchCondition.DEFAULT_DATE_RANGE,
            AdminVacationSearchCondition.DEFAULT_APPLICANT,
            vacationRequestStatus
        )
    }
}
