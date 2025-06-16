package programmers.team6.domain.admin.support

import lombok.EqualsAndHashCode
import programmers.team6.domain.vacation.entity.VacationInfoLog

@EqualsAndHashCode
class VacationInfoLogs(private val lastedMap: Map<Long, VacationInfoLog>) {
    fun findVacationInfo(id: Long): VacationInfoLog? = lastedMap[id]
}
