package programmers.team6.domain.vacation.entity

import jakarta.persistence.*
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.global.entity.BaseEntity
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException

@Entity
class ApprovalStep(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_step_id")
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @ManyToOne
    @JoinColumn(name = "vacation_request_id", nullable = false)
    var vacationRequest: VacationRequest,

    @Column(name = "approval_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    var approvalStatus: ApprovalStatus,

    val step: Int,

    var reason: String? = null

) : BaseEntity() {

    fun update(reason: String?) {
        this.reason = reason
    }

    private fun updateStatus(approvalStatus: ApprovalStatus) {
        this.approvalStatus = approvalStatus
    }

    fun approve() = updateStatus(ApprovalStatus.APPROVED)

    fun reject(reason: String?) {
        updateStatus(ApprovalStatus.REJECTED)
        this.reason = reason
    }

    fun pending() = updateStatus(ApprovalStatus.PENDING)

    fun cancel() = updateStatus(ApprovalStatus.CANCELED)

    // todo : approve()하고 결합하여 하나의 승인 로직으로 리펙토링
    fun validateApprovable() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_APPROVE)
        }
    }

    fun validateRejectable() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_REJECT)
        }
    }

    val vacationMemberId: Long
        get() = vacationRequest.memberId

    val vacationCode: String
        get() = vacationRequest.code

    fun calcVacationDays(): Int = vacationRequest.calcVacationDays()


    fun approveVacation() = vacationRequest.approve()


    fun rejectVacation() = vacationRequest.reject()


    fun cancelVacation() = vacationRequest.cancel()

    val isHalfDay: Boolean
        get() = vacationRequest.isHalfDay

    val isHrApprover: Boolean
        get() = member.isHrMember
}
