package programmers.team6.domain.vacation.support

import programmers.team6.domain.vacation.entity.VacationInfo

class VacationInfos(infos: List<VacationInfo>) {
    private val infos: Map<Long, List<VacationInfo>> = infos.groupBy { it.memberId }

    val memberIds: List<Long>
        get() = infos.keys.toList()

    fun getByMemberId(id: Long): List<VacationInfo> =
        infos[id] ?: emptyList()

    val all: List<VacationInfo>
        get() = infos.values.flatten()
}
