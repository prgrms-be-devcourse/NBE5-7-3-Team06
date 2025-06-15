package programmers.team6.domain.auth.dto

import programmers.team6.domain.member.enums.Role
import java.util.*


data class TokenBody(
	@JvmField val id: Long,
	@JvmField val name: String,
	@JvmField val role: Role,
	@JvmField val expiration: Date,
	@JvmField val issuedAt: Date
)
