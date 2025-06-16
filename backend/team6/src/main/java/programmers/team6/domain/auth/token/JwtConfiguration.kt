package programmers.team6.domain.auth.token

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(prefix = "jwt")
data class JwtConfiguration(
	val secret: String,
	val accessTokenExpiration: Long,
	val refreshTokenExpiration: Long,
	val header: String,
	val prefix: String
)
