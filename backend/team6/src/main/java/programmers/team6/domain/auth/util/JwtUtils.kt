package programmers.team6.domain.auth.util;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtils {

	public static long toSeconds(long millis) {
		return millis / 1000;
	}

	public static long calculateTtlMillis(Date expiration) {
		return Math.max(expiration.getTime() - System.currentTimeMillis(), 0);
	}

	public static void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long expiresIn) {
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.sameSite("Strict")
			.maxAge(JwtUtils.toSeconds(expiresIn))
			.build();

		response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
	}

}
