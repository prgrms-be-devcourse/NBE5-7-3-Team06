package programmers.team6.domain.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import programmers.team6.domain.auth.config.JwtConfiguration;
import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.auth.dto.TokenPairWithExpiration;
import programmers.team6.domain.auth.dto.response.AccessTokenResponse;
import programmers.team6.domain.auth.service.JwtService;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.exception.customException.UnauthorizedException;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTests {

    @Mock
    JwtService jwtService;

    JwtTokenProvider jwtTokenProvider;

    JwtConfiguration jwtConfiguration;

    SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtConfiguration = new JwtConfiguration(
                "test-test-test-test-test-test-test-test-test-1234567890",
                300_000L,
                1209600000,
                "Authorization",
                "Bearer"
        );
        jwtTokenProvider = new JwtTokenProvider(jwtConfiguration, jwtService);

        secretKey = Keys.hmacShaKeyFor(jwtConfiguration.secret().getBytes());
    }

    @Test
    @DisplayName("토큰을 정상적으로 생성한다.")
    void generate_tokenPair(){

        JwtMemberInfo jwtMemberInfo =  genJwtMemberInfo();

        TokenPairWithExpiration tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo);

        Claims accessToken = genClaims(tokenPair.accessToken());

        Claims refreshToken = genClaims(tokenPair.refreshToken());

        assertThat(accessToken.getSubject()).isEqualTo(jwtMemberInfo.id().toString());
        assertThat(accessToken.get("role")).isEqualTo(Role.USER.name());
        assertThat(accessToken.getExpiration()).isNotNull();

        assertThat(refreshToken.getSubject()).isEqualTo(jwtMemberInfo.id().toString());
        assertThat(refreshToken.get("role")).isEqualTo(Role.USER.name());
        assertThat(refreshToken.getExpiration()).isNotNull();

    }



    @Test
    @DisplayName("access token 을 재발급한다.")
    void generate_accessToken(){

        JwtMemberInfo jwtMemberInfo = genJwtMemberInfo();

        TokenPairWithExpiration tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo);

        String refreshToken = tokenPair.refreshToken();

        AccessTokenResponse accessTokenResponse = jwtTokenProvider.generateAccessToken(refreshToken);

        Claims claims = genClaims(accessTokenResponse.accessToken());

        assertThat(claims.getSubject()).isEqualTo(jwtMemberInfo.id().toString());
        assertThat(claims.get("role")).isEqualTo(Role.USER.name());
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    @DisplayName("토큰의 유효성을 검사한다 - 성공 ")
    void token_validate_success() {

        JwtMemberInfo jwtMemberInfo = genJwtMemberInfo();

        TokenPairWithExpiration tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo);

        String accessToken = tokenPair.accessToken();

        assertThatCode(
                () -> {
                    jwtTokenProvider.validate(accessToken);
                }
        ).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("토큰 유효성 검사 실패 - 서명이 잘못된 토큰 ")
    void token_validate_fail_signature() {

        JwtMemberInfo jwtMemberInfo = genJwtMemberInfo();

        JwtConfiguration wrongConfig = new JwtConfiguration(
                "this-is-a-wrong-secret-key-which-is-very-long-32-bytes",
                jwtConfiguration.accessTokenExpiration(),
                jwtConfiguration.refreshTokenExpiration(),
                jwtConfiguration.header(),
                jwtConfiguration.secret()
        );

        JwtTokenProvider wrongJwtTokenProvider = new JwtTokenProvider(wrongConfig, jwtService);

        TokenPairWithExpiration tokenPair = wrongJwtTokenProvider.generateTokenPair(jwtMemberInfo);

        String wrongToken = tokenPair.accessToken();

        assertThatThrownBy(
                () -> {
                    jwtTokenProvider.validate(wrongToken);
                }
        ).isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode",UnauthorizedErrorCode.UNAUTHORIZED_INVALID_SIGNATURE);
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 구조가 잘못된 토큰 ")
    void token_validate_fail_malformed() {

        String wrongToken = "not.jwt";

        assertThatThrownBy(
                () -> {
                    jwtTokenProvider.validate(wrongToken);
                }
        ).isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode",UnauthorizedErrorCode.UNAUTHORIZED_MALFORMED_TOKEN);
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 만료된 토큰 ")
    void token_validate_fail_expired() {

        JwtMemberInfo jwtMemberInfo = genJwtMemberInfo();

        JwtConfiguration wrongConfig = new JwtConfiguration(
                jwtConfiguration.secret(),
                1L,
                jwtConfiguration.refreshTokenExpiration(),
                jwtConfiguration.header(),
                jwtConfiguration.prefix()
        );

        JwtTokenProvider wrongJwtTokenProvider = new JwtTokenProvider(wrongConfig, jwtService);

        TokenPairWithExpiration tokenPair = wrongJwtTokenProvider.generateTokenPair(jwtMemberInfo);

        String wrongToken = tokenPair.accessToken();

        assertThatThrownBy(
                () -> {
                    jwtTokenProvider.validate(wrongToken);
                }
        ).isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode",UnauthorizedErrorCode.UNAUTHORIZED_EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 빈 문자열")
    void token_validate_fail_illegal() {

        assertThatThrownBy(
                () -> {
                    jwtTokenProvider.validate(" ");
                }
        ).isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode",UnauthorizedErrorCode.UNAUTHORIZED_ILLEGAL_ARGUMENT_TOKEN);
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 등록되어있지 않으면 아무것도 반환하지않는다.")
    void validate_not_blackListed_success() {

        String refreshToken = "refreshToken";

        when(jwtService.isBlackListed(refreshToken)).thenReturn(false);

        assertThatCode(
                () -> {
                    jwtTokenProvider.validateNotBlackListed(refreshToken);
                }
        ).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("refresh token이 블랙리스트에 등록되어있으면 예외를 반환한다.")
    void validate_not_blackListed_failure() {

        String expiredToken = "refreshToken";

        when(jwtService.isBlackListed(expiredToken)).thenReturn(true);

        assertThatThrownBy(
                () -> {
                    jwtTokenProvider.validateNotBlackListed(expiredToken);
                }
        ).isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorCode",UnauthorizedErrorCode.UNAUTHORIZED_BLACKLIST_TOKEN);
    }


    @Test
    @DisplayName("token을 넣으면 token body를 반환한다.")
    void parse_claims() {

        JwtMemberInfo jwtMemberInfo =  genJwtMemberInfo();
        TokenPairWithExpiration tokenPair = jwtTokenProvider.generateTokenPair(jwtMemberInfo);
        String token = tokenPair.accessToken();

        TokenBody tokenBody = jwtTokenProvider.parseClaims(token);

        assertThat(tokenBody)
                .extracting("id","name","role")
                .containsExactly(
                        jwtMemberInfo.id(),
                        jwtMemberInfo.name(),
                        jwtMemberInfo.role()
                );
        assertThat(tokenBody.expiration()).isNotNull();
        assertThat(tokenBody.issuedAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰 발급하기")
    void issue_token() {

        JwtMemberInfo jwtMemberInfo = genJwtMemberInfo();

        String accessToken = jwtTokenProvider.issueAccessToken(jwtMemberInfo);

        Claims claims = genClaims(accessToken);

        assertThat(claims.getSubject()).isEqualTo(jwtMemberInfo.id().toString());
        assertThat(claims.get("name")).isEqualTo(jwtMemberInfo.name());
        assertThat(claims.get("role")).isEqualTo(jwtMemberInfo.role().name());

        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();

    }

    @Test
    @DisplayName("request 에서 token 추출하기 - 성공")
    void extract_token_success()  {

        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "this.is.token";
        String header = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(header);

        String extractedToken = jwtTokenProvider.extractToken(request);

        assertThat(extractedToken).isEqualTo(token);

    }

    @Test
    @DisplayName("request 에서 token 추출하기 - 실패하면 null 반환")
    void extract_token_failure() {

        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "this.is.token";
        String wrongHeader = "wrong" + token;

        when(request.getHeader("Authorization")).thenReturn(wrongHeader);

        String extractedToken = jwtTokenProvider.extractToken(request);

        assertThat(extractedToken).isNull();

    }


    private JwtMemberInfo genJwtMemberInfo() {
        return new JwtMemberInfo(1L, "member1", Role.USER);
    }

    private Claims genClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}