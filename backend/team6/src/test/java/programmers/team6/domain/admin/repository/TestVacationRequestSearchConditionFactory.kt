package programmers.team6.domain.admin.repository;

import java.time.LocalDate;

import lombok.experimental.UtilityClass;
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition;
import programmers.team6.domain.admin.enums.Quarter;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

@UtilityClass
public class TestVacationRequestSearchConditionFactory {
	public static AdminVacationSearchCondition createByDateRange(LocalDate start, LocalDate end) {
		return new AdminVacationSearchCondition(
			AdminVacationSearchCondition.bindingDateRangeCondition(start, end, null, null), null, null);
	}

	public static AdminVacationSearchCondition createByDateRange(Integer year, Quarter quarter) {
		return new AdminVacationSearchCondition(
			AdminVacationSearchCondition.bindingDateRangeCondition(null, null, year, quarter), null, null);
	}

	public static AdminVacationSearchCondition createByApplicant(String name, String deptName) {
		return new AdminVacationSearchCondition(null,
			AdminVacationSearchCondition.bindingApplicantCondition(name, deptName, null, null), null);
	}

	public static AdminVacationSearchCondition createByApplicant(Long positionCodeId, Long vacationTypeCodeId) {
		return new AdminVacationSearchCondition(null,
			AdminVacationSearchCondition.bindingApplicantCondition(null, null, positionCodeId, vacationTypeCodeId),
			null);
	}

	public static AdminVacationSearchCondition createByVacationRequestStatus(
		VacationRequestStatus vacationRequestStatus) {
		return new AdminVacationSearchCondition(null, null, vacationRequestStatus);
	}

}
