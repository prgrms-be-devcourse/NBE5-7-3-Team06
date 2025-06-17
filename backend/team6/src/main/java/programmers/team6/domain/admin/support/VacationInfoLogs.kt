package programmers.team6.domain.admin.support

import programmers.team6.domain.vacation.entity.VacationInfoLog

class VacationInfoLogs(private val lastedMap: Map<Long, VacationInfoLog>) {

    fun findVacationInfo(id: Long): VacationInfoLog? = lastedMap[id]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VacationInfoLogs

        return lastedMap == other.lastedMap
    }

    override fun hashCode(): Int {
        return lastedMap.hashCode()
    }

}
