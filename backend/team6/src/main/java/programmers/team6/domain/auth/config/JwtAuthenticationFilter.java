package programmers.team6.domain.auth.config;

import static programmers.team6.global.exception.code.UnauthorizedErrorCode.*;
import static programmers.team6.global.util.ErrorResponseUtil.*;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.View;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.auth.token.JwtTokenProvider;
import programmers.team6.global.exception.customException.UnauthorizedException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	private static final List<String> TOKEN_FREE_URIS = List.of(
		"/auth", "/codes", "/depts"
	);
	private final View error;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String uri = request.getRequestURI();

		boolean tokenFree = TOKEN_FREE_URIS.stream().anyMatch(uri::startsWith);

		String token = jwtTokenProvider.extractToken(request);

		if (tokenFree) {
			filterChain.doFilter(request, response);
			return;
		}

		if (token == null) {
			setErrorResponse(response, UNAUTHORIZED_INVALID_HEADER);
			return;
		}

		try {
			jwtTokenProvider.validate(token);
		} catch (UnauthorizedException e) {
			setErrorResponse(response, e.getErrorCode());
			return;
		}

		TokenBody tokenbody = jwtTokenProvider.parseClaims(token);

		Authentication auth = new UsernamePasswordAuthenticationToken(
			tokenbody, null, List.of(new SimpleGrantedAuthority(tokenbody.role().toString()))
		);

		SecurityContextHolder.getContext().setAuthentication(auth);

		filterChain.doFilter(request, response);
	}

}
