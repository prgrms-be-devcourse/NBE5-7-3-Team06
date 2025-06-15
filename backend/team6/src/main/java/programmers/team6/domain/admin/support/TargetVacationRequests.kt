package programmers.team6.domain.admin.support

import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.global.util.DateUtil

class TargetVacationRequests(private val vacationRequests: List<VacationRequest>) {
    fun count(year: Int, month: Int): Double {
        return countFrom(year, month)
    }

    private fun countFrom(year: Int, month: Int): Double {
        var count = 0.0
        for (vacationRequest in vacationRequests) {
            if (isInRange(vacationRequest, month)) {
                count += calcVacationDays(vacationRequest, year, month)
            }
        }
        return count
    }

    private fun calcVacationDays(vacationRequest: VacationRequest, year: Int, month: Int): Double {
        val from = vacationRequest.from.toLocalDate()
        val to = vacationRequest.to.toLocalDate()
        if (vacationRequest.code == "05") {
            return 0.5
        }
        if (from.monthValue == to.monthValue) {
            return (DateUtil.calcDaysOfService(from, to) + 1).toDouble()
        }
        if (from.monthValue == month && from.year == year) {
            return (DateUtil.calcDaysOfService(from, DateUtil.lastDateFrom(from)) + 1).toDouble()
        }

        if (to.monthValue == month && to.year == year) {
            return (DateUtil.calcDaysOfService(to, DateUtil.startDateFrom(to)) + 1).toDouble()
        }
        return 0.0
    }

    private fun isInRange(vacationRequest: VacationRequest, month: Int): Boolean {
        val from = vacationRequest.from.toLocalDate()
        val to = vacationRequest.to.toLocalDate()
        if (from.monthValue == month || to.monthValue == month) {
            return true
        }
        return false
    }
}
