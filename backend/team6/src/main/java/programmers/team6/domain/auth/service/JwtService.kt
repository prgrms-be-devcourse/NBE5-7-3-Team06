package programmers.team6.domain.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final StringRedisTemplate stringRedisTemplate;

	private static final String PREFIX = "BL_";

	public void addBlackList(String refreshToken, long expirationTime) {
		String key = PREFIX + refreshToken;
		stringRedisTemplate.opsForValue().set(key, "logout", expirationTime, TimeUnit.MILLISECONDS);
	}

	public boolean isBlackListed(String refreshToken) {
		return stringRedisTemplate.hasKey(PREFIX + refreshToken);
	}

}
