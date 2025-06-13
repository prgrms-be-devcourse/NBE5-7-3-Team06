package programmers.team6.global.config;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.token.JwtTokenProvider;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static programmers.team6.support.JwtMemberInfoMother.admin;
import static programmers.team6.support.JwtMemberInfoMother.defaultUser;



@Slf4j
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    @Test
    @DisplayName("permitAll 경로는 토큰 없이 접근 가능하다")
    void access_without_token() throws Exception {

        String groupCode = "POSITION";

        mockMvc.perform(get("/codes/group/"+groupCode))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("admin 권한이 없으면 /admin/** 접근 불가")
    void access_without_admin_hasAuthority() throws Exception {

        JwtMemberInfo user = defaultUser();

        String token = jwtTokenProvider.generateTokenPair(user).accessToken();

        mockMvc.perform(get("/admin/member-approvals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codeName").value("FORBIDDEN_NO_AUTHORITY"));;
    }

    @Test
    @DisplayName("admin 권한이 있어야 /admin/** 접근 가능")
    void access_with_admin_hasAuthority() throws Exception {

        JwtMemberInfo admin = admin();

        String token = jwtTokenProvider.generateTokenPair(admin).accessToken();

        mockMvc.perform(get("/admin/member-approvals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

    }
    
    @Test
    @DisplayName("토큰없이 인증이 필요한 api 에 접근하면 예외를 반환한다.")
    void access_api_without_token() throws Exception {
    
        mockMvc.perform(get("/vacations"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codeName").value("UNAUTHORIZED_INVALID_HEADER"));
    
    }

    @Test
    @DisplayName("유효한 토큰으로 api 접근 성공 ")
    void access_api_with_token() throws Exception {

        JwtMemberInfo user = defaultUser();

        String token = jwtTokenProvider.generateTokenPair(user).accessToken();

        String yearMonth = "2025-05";
        String deptId = "1";

        mockMvc.perform(get("/vacations/calendar?yearMonth="+yearMonth+"&deptId="+deptId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    @Sql(scripts = "/data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void login_success_returns_token() throws Exception {

        String json = """
            {
                "email": "leader@dev.com",
                "password": "password1234"
            }
        """;

        MvcResult mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.token.accessTokenExpiresIn").isNotEmpty())
                .andExpect(jsonPath("$.token.id").value(1))
                .andExpect(jsonPath("$.token.name").value("리더"))
                .andExpect(jsonPath("$.token.role").value("ADMIN"))
                .andReturn();


        Cookie[] cookies = mvcResult.getResponse().getCookies();
        Optional<Cookie> refreshCookie = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst();

        assertThat(refreshCookie).isNotEmpty();
        assertThat(refreshCookie.get().getValue()).isNotBlank();
        assertThat(refreshCookie.get().getMaxAge()).isEqualTo(1209600);

    }

}