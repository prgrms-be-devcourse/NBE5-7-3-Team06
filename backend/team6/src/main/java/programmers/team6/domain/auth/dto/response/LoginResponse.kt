package programmers.team6.domain.auth.dto.response

@JvmRecord
data class LoginResponse(
    @JvmField val authTokenResponse: AuthTokenResponse,
    @JvmField val refreshToken: String,
    @JvmField val refreshTokenExpiresIn: Long
)
