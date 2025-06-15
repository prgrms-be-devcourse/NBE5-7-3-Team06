package programmers.team6.domain.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.admin.service.VacationStatisticsService;
import programmers.team6.domain.vacation.dto.response.VacationMonthlyStatisticsResponse;
import programmers.team6.global.paging.PagingConfig;

@RestController
@RequestMapping("/admin/vacations/statistics")
@RequiredArgsConstructor
public class VacationStatisticsController {

	private final VacationStatisticsService vacationStatisticsService;

	@GetMapping
	public Page<VacationMonthlyStatisticsResponse> monthlySummary(
		@Validated VacationStatisticsRequest request,
		@PagingConfig Pageable pageable) {
		return vacationStatisticsService.getMonthlyVacationStatistics(request, pageable);
	}
}
