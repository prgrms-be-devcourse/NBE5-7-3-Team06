package programmers.team6.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import programmers.team6.domain.auth.token.JwtAuthenticationFilter
import programmers.team6.global.exception.code.ForbiddenErrorCode
import programmers.team6.global.exception.code.UnauthorizedErrorCode
import programmers.team6.global.util.ErrorResponseUtil

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .httpBasic { httpB: HttpBasicConfigurer<HttpSecurity> -> httpB.disable() }
            .formLogin { form: FormLoginConfigurer<HttpSecurity> -> form.disable() }
            .csrf { csrf: CsrfConfigurer<HttpSecurity> -> csrf.disable() }
            .cors { cors: CorsConfigurer<HttpSecurity?> -> cors.configurationSource(corsConfigurationSource()) }
            .sessionManagement { session: SessionManagementConfigurer<HttpSecurity?> ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            }
            .authorizeHttpRequests(
                { auth->
                    auth
                        .requestMatchers(
                            "/auth/**",
                            "/codes/**",
                            "/depts/**"
                        )
                        .permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                }
            ).exceptionHandling { exception ->
                exception
                    .authenticationEntryPoint { _, response, _ ->
                        ErrorResponseUtil.setErrorResponse(response, UnauthorizedErrorCode.UNAUTHORIZED_ENTRY_POINT)
                    }
                    .accessDeniedHandler { _, response, _ ->
                        ErrorResponseUtil.setErrorResponse(response, ForbiddenErrorCode.FORBIDDEN_NO_AUTHORITY)
                    }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()

        config.allowedOrigins = listOf("http://localhost:3000")
        config.allowedMethods =
            listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")

        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return source
    }
}
