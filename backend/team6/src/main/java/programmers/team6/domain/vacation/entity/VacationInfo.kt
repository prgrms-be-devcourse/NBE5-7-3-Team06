package programmers.team6.domain.vacation.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.NoArgsConstructor
import org.springframework.lang.CheckReturnValue
import programmers.team6.global.entity.BaseEntity
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException

@Entity
class VacationInfo(
    var totalCount: Double,
    var useCount: Double,
    var vacationType: String,
    var memberId: Long?
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var vacationId: Int? = null
        protected set

    @Version
    var version: Int = 0
        protected set

    constructor(totalCount: Double, vacationType: String, memberId: Long?) : this(
        totalCount,
        0.0,
        vacationType,
        memberId
    )

    @CheckReturnValue
    fun updateTotalCount(totalCount: Double): VacationInfoLog {
        return update(totalCount, this.useCount)
    }

    @CheckReturnValue
    fun init(totalCount: Double): VacationInfoLog {
        return update(totalCount, 0.0)
    }

    @CheckReturnValue
    fun useVacation(count: Double): VacationInfoLog {
        return update(this.totalCount, this.useCount + count)
    }

    fun isSameVersion(version: Int): Boolean {
        return this.version == version
    }

    fun canUseVacation(count: Double): Boolean {
        return this.useCount + count <= this.totalCount
    }

    @CheckReturnValue
    private fun update(totalCount: Double, useCount: Double): VacationInfoLog {
        if (useCount > totalCount) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_INVALID_INPUT)
        }
        this.totalCount = totalCount
        this.useCount = useCount
        return toLog()
    }

    fun toLog(): VacationInfoLog {
        return VacationInfoLog.from(this)
    }

//    fun getVacationId(): Int {
//        return vacationId
//    }
//
//    fun getTotalCount(): Double {
//        return totalCount
//    }
//
//    fun getUseCount(): Double {
//        return useCount
//    }
//
//    fun getVacationType(): String? {
//        return vacationType
//    }
//
//    fun getMemberId(): Long? {
//        return memberId
//    }
//
//    fun getVersion(): Int {
//        return version
//    }
}
