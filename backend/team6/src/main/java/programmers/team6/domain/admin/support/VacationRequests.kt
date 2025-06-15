package programmers.team6.domain.admin.support

import programmers.team6.domain.vacation.entity.VacationRequest

class VacationRequests(requests: List<VacationRequest>) {
    private val requests: Map<Long, List<VacationRequest>>

    init {
        this.requests = requests.groupBy { it.memberId }
    }

    fun targetRequests(memberId: Long): TargetVacationRequests {
        return TargetVacationRequests(requests[memberId].orEmpty())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VacationRequests

        return requests == other.requests
    }

    override fun hashCode(): Int {
        return requests.hashCode()
    }

}
