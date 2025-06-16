package programmers.team6.domain.vacation.support

import org.springframework.stereotype.Component
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository

@Component
open class VacationInfoLogPublisher(private val vacationInfoRepository: VacationInfoLogRepository) {

    fun publish(vacationInfoLog: VacationInfoLog) {
        vacationInfoRepository.save(vacationInfoLog)
    }

    fun publish(logs: List<VacationInfoLog>) {
        vacationInfoRepository.saveAll(logs)
    }
}
