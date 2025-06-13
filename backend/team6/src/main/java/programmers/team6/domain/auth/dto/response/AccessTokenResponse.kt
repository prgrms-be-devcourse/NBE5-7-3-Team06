package programmers.team6.domain.auth.dto.response

@JvmRecord
data class AccessTokenResponse(
	@JvmField val accessToken: String,
	val accessTokenExpiresIn: Long
) 