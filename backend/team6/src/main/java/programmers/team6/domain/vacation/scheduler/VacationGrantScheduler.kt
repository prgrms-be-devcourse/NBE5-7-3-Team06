package programmers.team6.domain.vacation.scheduler

import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import programmers.team6.domain.vacation.service.VacationGrantService
import java.time.LocalDate

@Component
@RequiredArgsConstructor
@Slf4j
class VacationGrantScheduler(private val vacationGrantService: VacationGrantService) {

    @Scheduled(cron = "\${schedule.grant-cron}")
    fun grantJob() {
        vacationGrantService.grantAnnualVacations(LocalDate.now())
    }
}
