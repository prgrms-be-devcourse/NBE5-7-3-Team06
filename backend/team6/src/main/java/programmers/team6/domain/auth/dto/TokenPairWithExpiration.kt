package programmers.team6.domain.auth.dto


data class TokenPairWithExpiration(
	@JvmField val accessToken: String,
	@JvmField val refreshToken: String,
	@JvmField val accessTokenExpiresIn: Long,
	@JvmField val refreshTokenExpiresIn: Long
)

