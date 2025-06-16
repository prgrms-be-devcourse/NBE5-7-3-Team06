package programmers.team6.domain.auth.service

import io.mockk.*
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit


internal class JwtServiceTests {

    private val stringRedisTemplate: StringRedisTemplate = mockk<StringRedisTemplate>()

    private val valueOperations: ValueOperations<String, String> = mockk<ValueOperations<String, String>>()

    private var jwtService: JwtService = JwtService(stringRedisTemplate)

    @BeforeEach
    fun setUp() {
        jwtService = JwtService(stringRedisTemplate)
    }

    @Test
    @DisplayName("블랙리스트(redis)추가")
    fun add_blackList() {
        val token = "test-refresh-token"
        val expirationTime = 1000L
        val key = "BL_$token"

        every { stringRedisTemplate.opsForValue()}returns valueOperations
        every { valueOperations.set(key, "logout", expirationTime, TimeUnit.MILLISECONDS) } just Runs

        jwtService.addBlackList(token, expirationTime)

        verify(exactly = 1) {
            valueOperations.set(key, "logout", expirationTime, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 저장되어있는 경우 ")
    fun isBlackListed_true(){
        val token = "test-refresh-token"

        every { stringRedisTemplate.hasKey("BL_$token") } returns true

        val result = jwtService.isBlackListed(token)

        Assertions.assertThat(result).isTrue()
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 없는 경우 ")
    fun isBlackListed_false(){
        val token = "test-refresh-token"

        every { stringRedisTemplate.hasKey("BL_$token") } returns false

        val result = jwtService.isBlackListed(token)

        Assertions.assertThat(result).isFalse()
    }
}