package programmers.team6

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import programmers.team6.domain.auth.token.JwtConfiguration

@EnableJpaAuditing
@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
@EnableConfigurationProperties(JwtConfiguration::class)
class Team6Application

fun main(args: Array<String>) {
    runApplication<Team6Application>(*args)
}