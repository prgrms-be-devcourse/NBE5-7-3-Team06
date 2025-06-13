package programmers.team6.domain.auth.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.global.exception.customException.UnauthorizedException;
import programmers.team6.global.exception.response.ErrorResponse;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static programmers.team6.global.exception.code.UnauthorizedErrorCode.UNAUTHORIZED_INVALID_TOKEN;
import static programmers.team6.support.JwtMemberInfoMother.defaultUser;



@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private MockFilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        objectMapper = Jackson2ObjectMapperBuilder.json().build();
    }

    @Test
    @DisplayName("인증이 필요 없는 uri로 접근하면 doFilter로 넘어간다 ")
    void access_with_freeToken() throws Exception {

        request.setRequestURI("/codes/group/POSITION");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);

    }

    @Test
    @DisplayName("정상적인 token은 필터를 통과한다.")
    void valid_token_success() throws Exception {

        JwtMemberInfo jwtMemberInfo = defaultUser();
        String token = "token";

        TokenBody tokenBody = new TokenBody(
                jwtMemberInfo.id(),
                jwtMemberInfo.name(),
                jwtMemberInfo.role(),
                new Date(System.currentTimeMillis() + 1000 * 60 * 60),
                new Date()
        );

        request.setRequestURI("/vacations");

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractToken(request)).thenReturn(token);
        when(jwtTokenProvider.parseClaims(token)).thenReturn(tokenBody);

        doNothing()
                .when(jwtTokenProvider)
                .validate(token);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEmpty();
        verify(filterChain, times(1)).doFilter(request, response);

    }

    @Test
    @DisplayName("token이 null이면 ErorrReponse 를 반환한다.")
    void token_is_null_exception() throws Exception {

        request.setRequestURI("/vacations");

        request.addHeader("Authorization", "Bearer ");

        when(jwtTokenProvider.extractToken(request)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);

        assertEquals("UNAUTHORIZED_INVALID_HEADER", errorResponse.getCodeName());

    }

    @Test
    @DisplayName("token이 validate를 통과하지 못하면 errorResponse를 반환한다.")
    void token_is_invalid() throws Exception {

        String token = "token";

        request.setRequestURI("/vacations");

        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.extractToken(request)).thenReturn(token);

        doThrow(new UnauthorizedException(UNAUTHORIZED_INVALID_TOKEN))
                .when(jwtTokenProvider)
                .validate(token);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertEquals("UNAUTHORIZED_INVALID_TOKEN", errorResponse.getCodeName());

    }

}