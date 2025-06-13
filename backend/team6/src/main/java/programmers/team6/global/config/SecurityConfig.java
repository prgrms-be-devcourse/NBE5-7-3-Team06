package programmers.team6.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.auth.token.JwtAuthenticationFilter;
import programmers.team6.global.exception.code.ForbiddenErrorCode;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.util.ErrorResponseUtil;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.httpBasic(httpB -> httpB.disable())
			.formLogin(form -> form.disable())
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(
				auth -> auth
					.requestMatchers(
						"/auth/**",
						"/codes/**",
						"/depts/**"
					)
					.permitAll()
					.requestMatchers("/admin/**").hasAuthority("ADMIN")
					.anyRequest().authenticated()
			).exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					ErrorResponseUtil.setErrorResponse(response, UnauthorizedErrorCode.UNAUTHORIZED_ENTRY_POINT);
				})
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					ErrorResponseUtil.setErrorResponse(response, ForbiddenErrorCode.FORBIDDEN_NO_AUTHORITY);
				})
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of("http://localhost:3000"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

}
