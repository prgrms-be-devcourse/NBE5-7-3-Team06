package programmers.team6.domain.auth.dto


data class TokenPairWithExpiration(
	val accessToken: String,
	val refreshToken: String,
	val accessTokenExpiresIn: Long,
	val refreshTokenExpiresIn: Long
)

