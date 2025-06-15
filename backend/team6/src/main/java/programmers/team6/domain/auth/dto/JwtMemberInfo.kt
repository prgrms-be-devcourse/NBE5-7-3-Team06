package programmers.team6.domain.auth.dto

import programmers.team6.domain.member.enums.Role


data class JwtMemberInfo(
    val id: Long,
    val name: String,
    val role: Role
)
