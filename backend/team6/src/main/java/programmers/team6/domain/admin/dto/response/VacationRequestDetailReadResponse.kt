package programmers.team6.domain.admin.dto.response

import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

data class VacationRequestDetailReadResponse(
    val id: Long,
    val from: LocalDateTime,
    val to: LocalDateTime,
    val memberId: Long,
    val name: String,
    val deptName: String,
    val position: String,
    val reason: String,
    val vacationType: String,
    val vacationRequestStatus: VacationRequestStatus,
    var approvalStepDetailUpdateResponses: List<ApprovalStepDetailUpdateResponse> = emptyList()
) {
    constructor(
        id: Long,
        from: LocalDateTime,
        to: LocalDateTime,
        memberId: Long,
        name: String,
        deptName: String,
        position: String,
        reason: String,
        vacationType: String,
        vacationRequestStatus: VacationRequestStatus
    ) : this(id, from, to, memberId, name, deptName, position, reason, vacationType, vacationRequestStatus, emptyList())




    fun injectApprovalStepDetails(
        approvalStepDetailUpdateResponses: List<ApprovalStepDetailUpdateResponse>
    ): VacationRequestDetailReadResponse {
        this.approvalStepDetailUpdateResponses = approvalStepDetailUpdateResponses

        return this
    }
}
