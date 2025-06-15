package programmers.team6.mock

import io.mockk.mockk
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.support.VacationInfoLogReader
import programmers.team6.domain.admin.support.VacationInfoLogs
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository
import java.util.*

class VacationInfoLogReaderFake(vararg logs: VacationInfoLog) :
    VacationInfoLogReader(mockk<VacationInfoLogRepository>()) {

    private val vacationInfoLogs: Map<Long, VacationInfoLog> = logs.associateBy { it.memberId }

    override fun lastedLogsFrom(ids: List<Long>, request: VacationStatisticsRequest): VacationInfoLogs {
        return VacationInfoLogs(vacationInfoLogs)
    }
}