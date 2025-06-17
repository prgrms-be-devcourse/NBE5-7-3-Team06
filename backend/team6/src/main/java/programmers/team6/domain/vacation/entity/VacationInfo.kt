package programmers.team6.domain.vacation.entity

import jakarta.persistence.*
import org.springframework.lang.CheckReturnValue
import programmers.team6.global.entity.BaseEntity
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException

@Entity
class VacationInfo(

    var totalCount: Double,

    var useCount: Double,

    var vacationType: String,

    var memberId: Long

) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val vacationId: Int? = null

    @Version
    val version: Int = 0

    constructor(totalCount: Double, vacationType: String, memberId: Long) : this(
        totalCount, 0.0, vacationType, memberId
    )

    @CheckReturnValue
    fun updateTotalCount(newCount: Double): VacationInfoLog =
        update(newCount, useCount)

    @CheckReturnValue
    fun init(newCount: Double): VacationInfoLog =
        update(newCount, 0.0)

    @CheckReturnValue
    fun useVacation(count: Double): VacationInfoLog =
        update(totalCount, useCount + count)

    fun isSameVersion(version: Int): Boolean =
        this.version == version

    fun canUseVacation(count: Double): Boolean =
        useCount + count <= totalCount

    @CheckReturnValue
    private fun update(newTotal: Double, newUsed: Double): VacationInfoLog {
        if (newUsed > newTotal) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_INVALID_INPUT)
        }
        this.totalCount = newTotal
        this.useCount = newUsed
        return toLog()
    }

    fun toLog(): VacationInfoLog =
        VacationInfoLog.from(this)
}
