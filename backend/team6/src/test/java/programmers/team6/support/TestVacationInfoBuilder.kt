package programmers.team6.support

import programmers.team6.domain.vacation.entity.VacationInfo

class TestVacationInfoBuilder {
    private var totalCount: Double = 0.0
    private lateinit var vacationType: String
    private var memberId: Long = 0L

    fun totalCount(totalCount: Double): TestVacationInfoBuilder {
        this.totalCount = totalCount
        return this
    }

    fun vacationType(vacationType: String): TestVacationInfoBuilder {
        this.vacationType = vacationType
        return this
    }

    fun memberId(memberId: Long): TestVacationInfoBuilder {
        this.memberId = memberId
        return this
    }

    fun build(): VacationInfo {
        return VacationInfo(totalCount, vacationType, memberId)
    }
}
