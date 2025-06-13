package programmers.team6.domain.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import programmers.team6.domain.admin.enums.Quarter;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;

public record AdminVacationSearchCondition(
	@Valid DateRangeCondition dateRange,
	@Valid ApplicantCondition applicant,
	// 휴가 신청 상태
	VacationRequestStatus vacationRequestStatus
) {
	public AdminVacationSearchCondition {
		if (dateRange == null) {
			dateRange = new DateRangeCondition(null, null, null, null);
		}
		if (applicant == null) {
			applicant = new ApplicantCondition(null, null, null, null);
		}
	}

	public record DateRangeCondition(
		// 시작일
		@FutureOrPresent
		LocalDateTime start,
		// 종료일
		@FutureOrPresent
		LocalDateTime end,
		// 년도
		@Min(2000)
		@Max(2100)
		Integer year,
		// 분기
		Quarter quarter
	) {
		public DateRangeCondition {
			if ((start != null && end == null) || (start == null && end != null) || (quarter != null && year == null)) {
				throw new BadRequestException(BadRequestErrorCode.BAD_REQUEST_VALIDATION);
			}
			if (quarter == null) {
				quarter = Quarter.NONE;
			}
		}
	}

	// 휴가 신청자
	public record ApplicantCondition(
		// 이름
		@Size(max = 30)
		String name,
		// 부서명
		@Size(max = 50)
		String deptName,
		// 직책 codeId
		@Positive
		Long positionCodeId,
		// 휴가 종류 codeId
		@Positive
		Long vacationTypeCodeId
	) {
	}

	public static DateRangeCondition bindingDateRangeCondition(LocalDate start, LocalDate end, Integer year,
		Quarter quarter) {
		return new DateRangeCondition(start != null ? start.atStartOfDay() : null,
			end != null ? end.atTime(23, 59, 59) : null, year, quarter);
	}

	public static ApplicantCondition bindingApplicantCondition(String name,
		String deptName,
		Long positionCodeId,
		Long vacationTypeCodeId) {
		return new ApplicantCondition(name, deptName, positionCodeId, vacationTypeCodeId);
	}

}
