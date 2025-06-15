package programmers.team6.domain.admin.support

import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository
import java.time.LocalDateTime

@Component
@RequiredArgsConstructor
open class VacationInfoLogReader(private val repository: VacationInfoLogRepository) {

    open fun lastedLogsFrom(ids: List<Long>, request: VacationStatisticsRequest): VacationInfoLogs {
        val date = LocalDateTime.of(request.year, 12, 31, 23, 59)
        val lastedByMemberIdInAndYear = repository.findLastedByMemberIdInAndYear(ids, date, request.vacationCode)
        return VacationInfoLogs(toLastedMap(lastedByMemberIdInAndYear))
    }

    private fun toLastedMap(logs: List<VacationInfoLog>): Map<Long, VacationInfoLog> {
        return logs.groupBy { it.getMemberId() }
            .mapValues { (_, logs) -> logs.reduce { acc, log -> lastedLog(acc, log) } }
    }

    private fun lastedLog(log1: VacationInfoLog, log2: VacationInfoLog): VacationInfoLog =
        if (log1.logDate.isAfter(log2.logDate)) log1 else log2
}
