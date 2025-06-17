package programmers.team6.domain.vacation.entity

import jakarta.persistence.*
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.entity.BaseEntity
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
class VacationRequest(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(name = "from_date", nullable = false)
    var from: LocalDateTime,

    @Column(name = "to_date", nullable = false)
    var to: LocalDateTime,

    var reason: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_code")
    var type: Code,

    @Enumerated(EnumType.STRING)
    var status: VacationRequestStatus = VacationRequestStatus.IN_PROGRESS,

    @Version
    var version: Int? = null
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_request_id")
    val id: Long = 0L

    companion object {
        fun create(
            member: Member,
            from: LocalDateTime,
            to: LocalDateTime,
            reason: String,
            type: Code,
            status: VacationRequestStatus = VacationRequestStatus.IN_PROGRESS,
            version: Int? = null
        ): VacationRequest {
            return VacationRequest(member, from, to, reason, type, status, version)
        }
    }

    fun update(
        type: Code,
        from: LocalDateTime,
        to: LocalDateTime,
        status: VacationRequestStatus,
        reason: String
    ) {
        this.type = type
        this.from = from
        this.to = to
        this.status = status
        this.reason = reason
    }

    fun updateStatus(vacationRequestStatus: VacationRequestStatus) {
        this.status = vacationRequestStatus
    }

    // UPDATE
    // 현재 요청자가 수정 권한을 가지고 있는지 확인
    fun canUpdate(memberId: Long): Boolean {
        return member.id == memberId && status == VacationRequestStatus.IN_PROGRESS
    }

    // 수정 권한 검증
    private fun validateUpdate(memberId: Long) {
        if (!canUpdate(memberId)) {
            // 세부 오류 메시지
            if (member.id != memberId) {
                throw RuntimeException("휴가 신청자만 수정할 수 있습니다.")
            }

            if(status != VacationRequestStatus.IN_PROGRESS) {
                throw IllegalStateException("진행 중인 휴가 요청만 수정할 수 있습니다.")
            }
        }
    }

    // 휴가 수정 권한 검증 후 수정 처리
    fun updateByMember(
        memberId: Long,
        from: LocalDateTime,
        to: LocalDateTime,
        reason: String,
        type: Code
    ) {
        validateUpdate(memberId)
        this.from = from
        this.to = to
        this.reason = reason
        this.type = type
    }

    // DELETE
    // 현재 요청자가 취소 권한을 가지고 있는지 학인
    fun canCancel(memberId: Long): Boolean {
        return member.id == memberId && status == VacationRequestStatus.IN_PROGRESS
    }

    // 취소 권한 확인
    private fun validateCancel(memberId: Long) {
        if (!canCancel(memberId)) {
            if (member.id != memberId) {
                throw RuntimeException("휴가 신청자만 취소할 수 있습니다.")
            }

            if (status == VacationRequestStatus.CANCELED) {
                throw IllegalStateException("이미 취소된 휴가 신청입니다.")
            }

            throw IllegalStateException("진행 중인 휴가 요청만 취소할 수 있습니다.")
        }
    }

    // 휴가 신청 취소
    private fun changeStatusToCanceled() {
        status = VacationRequestStatus.CANCELED
    }

    // 휴가 취소 권한 검증 후 취소 처리
    fun validateAndCancel(memberId: Long) {
        validateCancel(memberId)
        changeStatusToCanceled()
    }

    fun approve() {
        updateStatus(VacationRequestStatus.APPROVED)
    }

    fun reject() {
        updateStatus(VacationRequestStatus.REJECTED)
    }

    fun cancel() {
        updateStatus(VacationRequestStatus.CANCELED)
    }

    fun calcVacationDays(): Int {
        return ChronoUnit.DAYS.between(from.toLocalDate(), to.toLocalDate()).toInt() + 1
    }

    val memberId: Long
        get() = this.member.id!!

    val code: String
        get() = this.type.code

    fun isHalfDay(): Boolean {
        return this.type.name == "반차"
    }
}