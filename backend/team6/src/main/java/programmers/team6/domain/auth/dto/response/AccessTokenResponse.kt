package programmers.team6.domain.auth.dto.response


data class AccessTokenResponse(
	@JvmField val accessToken: String,
	@JvmField val accessTokenExpiresIn: Long
) 