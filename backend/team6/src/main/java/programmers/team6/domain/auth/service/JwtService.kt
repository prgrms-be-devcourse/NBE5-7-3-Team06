package programmers.team6.domain.auth.service

import lombok.RequiredArgsConstructor
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class JwtService(
    private val stringRedisTemplate: StringRedisTemplate
) {

    companion object {
        private const val PREFIX = "BL_"
    }

    fun addBlackList(refreshToken: String, expirationTime: Long) {
        val key = PREFIX + refreshToken
        stringRedisTemplate.opsForValue()[key, "logout", expirationTime] = TimeUnit.MILLISECONDS
    }

    fun isBlackListed(refreshToken: String): Boolean {
        return stringRedisTemplate.hasKey(PREFIX + refreshToken)
    }


}
