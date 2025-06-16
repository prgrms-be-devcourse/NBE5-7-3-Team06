package programmers.team6.mock

import io.mockk.mockk
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher

class VacationInfoLogPublisherFake : VacationInfoLogPublisher(mockk<VacationInfoLogRepository>()) {
    private val vacationInfoLogs: MutableList<VacationInfoLog> = mutableListOf()

    override fun publish(logs: List<VacationInfoLog>) {
        vacationInfoLogs.addAll(logs)
    }

    fun isSameInput(logs: List<VacationInfoLog>): Boolean {
        for (log in logs) {
            if (!contains(log)) {
                return false
            }
        }
        return true
    }

    private fun contains(log: VacationInfoLog): Boolean {
        for (vacationInfoLog in vacationInfoLogs) {
            if (isSameInput(vacationInfoLog, log)) {
                return true
            }
        }
        return false
    }

    private fun isSameInput(log1: VacationInfoLog, log2: VacationInfoLog): Boolean {
        if (log1.memberId != log2.memberId) {
            return false
        }
        if (log1.vacationType != log2.vacationType) {
            return false
        }
        if (log1.totalCount != log2.totalCount) {
            return false
        }
        if (log1.useCount != log2.useCount) {
            return false
        }
        return true
    }
}
