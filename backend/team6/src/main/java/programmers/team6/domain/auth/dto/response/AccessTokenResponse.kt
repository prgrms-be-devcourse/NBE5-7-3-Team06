package programmers.team6.domain.auth.dto.response


data class AccessTokenResponse(
	val accessToken: String,
	val accessTokenExpiresIn: Long
) 