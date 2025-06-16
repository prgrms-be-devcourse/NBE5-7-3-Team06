package programmers.team6.domain.admin.dto.response

data class MemberApprovalResponse(
    val memberId: Long,
    val name: String,
    val positionName: String,
    val deptName: String,
    val birth: String,
    val email: String
)
