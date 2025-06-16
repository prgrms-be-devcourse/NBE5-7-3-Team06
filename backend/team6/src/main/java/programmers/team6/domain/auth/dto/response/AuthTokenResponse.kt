package programmers.team6.domain.auth.dto.response

import programmers.team6.domain.member.enums.Role


data class AuthTokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val id: Long,
    val name: String,
    val role: Role
)
