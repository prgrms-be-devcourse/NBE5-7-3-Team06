package programmers.team6.domain.admin.dto.response

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import programmers.team6.domain.admin.enums.Quarter
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import java.time.LocalDate
import java.time.LocalDateTime

data class AdminVacationSearchCondition(
    @Valid
    @JsonSetter(nulls = Nulls.SKIP)
    val dateRange: DateRangeCondition = DEFAULT_DATE_RANGE,
    @Valid
    @JsonSetter(nulls = Nulls.SKIP)
    val applicant: ApplicantCondition = DEFAULT_APPLICANT,

    val vacationRequestStatus: VacationRequestStatus?
) {

    class DateRangeCondition(
        val start: LocalDateTime?,
        val end: LocalDateTime?,
        @Max(2100) @Min(2000)
        val year: Int?,
        val quarter: Quarter
    ) {
        init {
            if ((start != null && end == null) || (start == null && end != null) || (quarter != Quarter.NONE && year == null)) {
                throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_VALIDATION)
            }
        }
    }

    // 휴가 신청자
    data class ApplicantCondition(
        @Size(max = 30)
        val name: String?,
        @Size(max = 50)
        val deptName: String?,
        @Positive
        val positionCodeId: Long?,
        @Positive
        val vacationTypeCodeId: Long?
    )

    companion object {
        @JvmStatic
        fun bindingDateRangeCondition(
            start: LocalDate?, end: LocalDate?, year: Int?,
            quarter: Quarter?
        ): DateRangeCondition {
            return DateRangeCondition(
                start?.atStartOfDay(),
                end?.atTime(23, 59, 59), year, (quarter ?: Quarter.NONE)
            )
        }

        @JvmStatic
        fun bindingApplicantCondition(
            name: String?,
            deptName: String?,
            positionCodeId: Long?,
            vacationTypeCodeId: Long?
        ): ApplicantCondition {
            return ApplicantCondition(name, deptName, positionCodeId, vacationTypeCodeId)
        }

        @JvmStatic
        val DEFAULT_DATE_RANGE = bindingDateRangeCondition(null, null, null, Quarter.NONE)
        @JvmStatic
        val DEFAULT_APPLICANT = bindingApplicantCondition(null, null, null, null)

    }
}
