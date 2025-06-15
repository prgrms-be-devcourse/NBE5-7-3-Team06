package programmers.team6.domain.auth.util

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import java.util.*
import kotlin.math.max


object JwtUtils {
    @JvmStatic
    fun toSeconds(millis: Long): Long {
        return millis / 1000
    }
    @JvmStatic
	fun calculateTtlMillis(expiration: Date): Long {
        return max((expiration.time - System.currentTimeMillis()).toDouble(), 0.0).toLong()
    }

    @JvmStatic
    fun addRefreshTokenCookie(response: HttpServletResponse, refreshToken: String, expiresIn: Long) {
        val refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(toSeconds(expiresIn))
            .build()

        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())
    }
}
