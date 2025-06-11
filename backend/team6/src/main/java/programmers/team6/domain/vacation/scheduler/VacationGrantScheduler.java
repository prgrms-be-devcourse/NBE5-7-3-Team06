package programmers.team6.domain.vacation.scheduler;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.vacation.service.VacationGrantService;

@Component
@RequiredArgsConstructor
@Slf4j
public class VacationGrantScheduler {

	private final VacationGrantService vacationGrantService;

	@Scheduled(cron = "${schedule.grant-cron}")
	public void grantJob() {
		vacationGrantService.grantAnnualVacations(LocalDate.now());
	}
}
