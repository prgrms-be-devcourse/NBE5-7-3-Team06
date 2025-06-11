package programmers.team6.domain.admin.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.AdminVacationRequestSearchResponse;
import programmers.team6.domain.admin.dto.AdminVacationSearchCondition;
import programmers.team6.domain.admin.dto.VacationRequestDetailReadResponse;
import programmers.team6.domain.admin.dto.VacationRequestDetailUpdateRequest;
import programmers.team6.domain.admin.enums.Quarter;
import programmers.team6.domain.admin.service.AdminService;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
	private final AdminService adminService;

	@GetMapping("/vacation-request")
	@ResponseStatus(HttpStatus.OK)
	AdminVacationRequestSearchResponse selectVacationRequests(
		@PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
		@RequestParam(value = "start", required = false) LocalDate startDate,
		@RequestParam(value = "end", required = false) LocalDate endDate,
		@RequestParam(value = "year", required = false) Integer year,
		@RequestParam(value = "quarter", required = false) Quarter quarter,

		// 신청자 파라미터
		@RequestParam(value = "name", required = false) String name,
		@RequestParam(value = "deptName", required = false) String deptName,
		@RequestParam(value = "positionCodeId", required = false) Long positionCodeId,
		@RequestParam(value = "vacationTypeCodeId", required = false) Long vacationTypeCodeId,

		// 휴가 신청 상태
		@RequestParam(value = "vacationRequestStatus", required = false) VacationRequestStatus status) {
		return adminService.search(pageable, new AdminVacationSearchCondition(
			AdminVacationSearchCondition.bindingDateRangeCondition(startDate, endDate, year, quarter),
			AdminVacationSearchCondition.bindingApplicantCondition(name, deptName, positionCodeId, vacationTypeCodeId),
			status
		));
	}

	@GetMapping("/vacation-request/{id}")
	@ResponseStatus(HttpStatus.OK)
	VacationRequestDetailReadResponse showVacationRequestDetail(@PathVariable Long id) {
		return adminService.selectVacationRequestDetailById(id);
	}

	@PutMapping("/vacation-request/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void updateVacationRequestDetail(@PathVariable Long id,
		@RequestBody @Valid VacationRequestDetailUpdateRequest vacationRequestDetailUpdateRequest) {
		adminService.updateVacationRequestDetailById(id, vacationRequestDetailUpdateRequest);
	}
}
