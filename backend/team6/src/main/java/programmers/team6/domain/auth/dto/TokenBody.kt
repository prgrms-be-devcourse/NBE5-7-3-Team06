package programmers.team6.domain.auth.dto

import programmers.team6.domain.member.enums.Role
import java.util.*


data class TokenBody(
	val id: Long,
	val name: String,
	val role: Role,
	val expiration: Date,
	val issuedAt: Date
)
