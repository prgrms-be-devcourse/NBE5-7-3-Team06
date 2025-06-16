package programmers.team6.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTests {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String,String> valueOperations;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(stringRedisTemplate);
    }
    
    @Test
    @DisplayName("블랙리스트(redis)추가")
    void add_blackList() {

        String token = "test-refresh-token";
        long expirationTime = 1000L;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        jwtService.addBlackList(token, expirationTime);

        verify(valueOperations).set("BL_" + token, "logout", expirationTime, TimeUnit.MILLISECONDS);

    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 저장되어있는 경우 ")
    void isBlackListed_true()  {

        String token = "test-refresh-token";
        when(stringRedisTemplate.hasKey("BL_" + token)).thenReturn(true);

        boolean result = jwtService.isBlackListed(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 없는 경우 ")
    void isBlackListed_false() {

        String token = "test-refresh-token";
        when(stringRedisTemplate.hasKey("BL_" + token)).thenReturn(false);

        boolean result = jwtService.isBlackListed(token);

        assertThat(result).isFalse();
    }
}