package programmers.team6.domain.vacation.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class VacationInfoLog(
    val totalCount: Double,
    val useCount: Double,
    val vacationType: String,
    val memberId: Long,
    val logDate: LocalDateTime
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    constructor(totalCount: Double, useCount: Double, vacationType: String, memberId: Long) : this(
        totalCount,
        useCount,
        vacationType,
        memberId,
        LocalDateTime.now()
    )

    fun isSameMemberId(memberId: Long): Boolean {
        return this.memberId == memberId
    }

    fun remainingCount(): Double {
        return totalCount - useCount
    }

    companion object {
        fun from(vacationInfo: VacationInfo): VacationInfoLog {
            return VacationInfoLog(
                vacationInfo.totalCount, vacationInfo.useCount,
                vacationInfo.vacationType, vacationInfo.memberId
            )
        }
    }
}
