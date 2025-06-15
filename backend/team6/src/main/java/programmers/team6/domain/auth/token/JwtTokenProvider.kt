package programmers.team6.domain.auth.token;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.auth.dto.TokenPairWithExpiration;
import programmers.team6.domain.auth.dto.response.AccessTokenResponse;
import programmers.team6.domain.auth.service.JwtService;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.exception.customException.UnauthorizedException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtConfiguration jwtConfiguration;
	private final JwtService jwtService;

	private static final String HEADER = "Authorization";
	private static final String BEARER = "Bearer ";

	public TokenPairWithExpiration generateTokenPair(JwtMemberInfo jwtMemberInfo) {

		String accessToken = issueAccessToken(jwtMemberInfo);
		String refreshToken = issueRefreshToken(jwtMemberInfo);

		return new TokenPairWithExpiration(accessToken, refreshToken, jwtConfiguration.accessTokenExpiration(),
			jwtConfiguration.refreshTokenExpiration());
	}

	public AccessTokenResponse generateAccessToken(String refreshToken) {

		TokenBody tokenBody = parseClaims(refreshToken);

		JwtMemberInfo jwtMemberInfo = new JwtMemberInfo(tokenBody.id(), tokenBody.name(), tokenBody.role());

		String accessToken = issueAccessToken(jwtMemberInfo);

		return new AccessTokenResponse(accessToken, jwtConfiguration.accessTokenExpiration());
	}

	public void validate(String token) {
		try {
			Jws<Claims> claimsJws = Jwts.parser()
				.verifyWith(getSecretKey())
				.build()
				.parseSignedClaims(token);
		} catch (SecurityException | SignatureException e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_SIGNATURE);
		} catch (MalformedJwtException e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_MALFORMED_TOKEN);
		} catch (ExpiredJwtException e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_EXPIRED_TOKEN);
		} catch (UnsupportedJwtException e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_UNSUPPORTED_TOKEN);
		} catch (IllegalArgumentException e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_ILLEGAL_ARGUMENT_TOKEN);
		} catch (Exception e) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_TOKEN);
		}
	}

	public void validateNotBlackListed(String refreshToken) {
		if (jwtService.isBlackListed(refreshToken)) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_BLACKLIST_TOKEN);
		}
	}

	public TokenBody parseClaims(String token) {

		Jws<Claims> claims = Jwts.parser()
			.verifyWith(getSecretKey())
			.build()
			.parseSignedClaims(token);

		Claims payload = claims.getPayload();

		Long id = Long.parseLong(payload.getSubject());

		return new TokenBody(
			id,
			payload.get("name").toString(),
			Role.valueOf(payload.get("role").toString()),
			payload.getExpiration(),
			payload.getIssuedAt());
	}

	public String issueAccessToken(JwtMemberInfo jwtMemberInfo) {
		return issue(jwtMemberInfo, jwtConfiguration.accessTokenExpiration());
	}

	public String issueRefreshToken(JwtMemberInfo jwtMemberInfo) {
		return issue(jwtMemberInfo, jwtConfiguration.refreshTokenExpiration());
	}

	private String issue(JwtMemberInfo jwtMemberInfo, Long expTime) {
		return Jwts.builder()
			.subject(jwtMemberInfo.id().toString())
			.claim("name", jwtMemberInfo.name())
			.claim("role", jwtMemberInfo.role())
			.issuedAt(new Date())
			.expiration(new Date(new Date().getTime() + expTime))
			.signWith(getSecretKey(), Jwts.SIG.HS256)
			.compact();
	}

	private SecretKey getSecretKey() {
		return Keys.hmacShaKeyFor(jwtConfiguration.secret().getBytes());
	}

	public String extractToken(HttpServletRequest request) {
		String header = request.getHeader(HEADER);

		if (header != null && header.startsWith(BEARER)) {
			return header.substring(BEARER.length());
		}
		return null;
	}

}
