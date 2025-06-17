package programmers.team6.domain.auth.token

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class JwtConfigurationTests {

    @Autowired(required = false)
    lateinit var jwtConfiguration: JwtConfiguration

    @Test
    @DisplayName("jwtConfiguration test")
    fun jwtConfiguration_test() {
        Assertions.assertThat(jwtConfiguration).isNotNull()
        Assertions.assertThat(jwtConfiguration.secret).isNotNull()
    }
}