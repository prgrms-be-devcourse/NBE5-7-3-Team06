package programmers.team6.domain.member.dto.response

@JvmRecord
data class MemberLoginInfoResponse(
    val id: Long,
    val name: String,
    val deptId: Long,
    val deptName: String,
    val positionId: Long,
    val positionName: String
)
