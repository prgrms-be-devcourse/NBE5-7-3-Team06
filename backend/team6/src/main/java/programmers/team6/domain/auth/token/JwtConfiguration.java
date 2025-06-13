package programmers.team6.domain.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConfigurationProperties(prefix = "jwt")
public record JwtConfiguration(
	String secret,
	long accessTokenExpiration,
	long refreshTokenExpiration,
	String header,
	String prefix
) {
}
