package programmers.team6.global.config

import jakarta.servlet.http.Cookie
import lombok.extern.slf4j.Slf4j
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.auth.token.JwtTokenProvider
import programmers.team6.support.JwtMemberInfoMother
import java.util.*

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
internal class SecurityConfigTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider


    @Test
    @DisplayName("permitAll 경로는 토큰 없이 접근 가능하다")
    @Throws(Exception::class)
    fun access_without_token() {
        val groupCode = "POSITION"

        mockMvc.perform(MockMvcRequestBuilders.get("/codes/group/$groupCode"))
            .andExpect(MockMvcResultMatchers.status().isOk())
    }


    @Test
    @DisplayName("admin 권한이 없으면 /admin/** 접근 불가")
    @Throws(Exception::class)
    fun access_without_admin_hasAuthority() {
        val user = JwtMemberInfoMother.defaultUser()

        val token = jwtTokenProvider.generateTokenPair(user).accessToken

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/member-approvals")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.codeName").value("FORBIDDEN_NO_AUTHORITY"))
    }

    @Test
    @DisplayName("admin 권한이 있어야 /admin/** 접근 가능")
    @Throws(Exception::class)
    fun access_with_admin_hasAuthority() {
        val admin = JwtMemberInfoMother.admin()

        val token = jwtTokenProvider.generateTokenPair(admin).accessToken

        mockMvc.perform(
            MockMvcRequestBuilders.get("/admin/member-approvals")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @DisplayName("토큰없이 인증이 필요한 api 에 접근하면 예외를 반환한다.")
    @Throws(Exception::class)
    fun access_api_without_token() {
        mockMvc.perform(MockMvcRequestBuilders.get("/vacations"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.codeName").value("UNAUTHORIZED_INVALID_HEADER"))
    }

//    @Test
//    @DisplayName("유효한 토큰으로 api 접근 성공 ")
//    @Throws(Exception::class)
//    fun access_api_with_token() {
//        val user = JwtMemberInfoMother.defaultUser()
//
//        val token = jwtTokenProvider.generateTokenPair(user).accessToken
//
//        val yearMonth = "2025-05"
//        val deptId = "1"
//
//        mockMvc.perform(
//            MockMvcRequestBuilders.get("/vacations/calendar?yearMonth=$yearMonth&deptId=$deptId")
//                .header("Authorization", "Bearer $token")
//        )
//            .andExpect(MockMvcResultMatchers.status().isOk())
//    }


    @Test
    @DisplayName("로그인 성공 시 토큰 반환")
    @Sql(scripts = ["/data-test.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Throws(
        Exception::class
    )
    fun login_success_returns_token() {
        val json = """
            {
                "email": "leader@dev.com",
                "password": "password1234"
            }
        
        """.trimIndent()

        val mvcResult = mockMvc.perform(
            MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.token.accessToken").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.token.accessTokenExpiresIn").isNotEmpty())
            .andExpect(MockMvcResultMatchers.jsonPath("$.token.id").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token.name").value("리더"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.token.role").value("ADMIN"))
            .andReturn()


        val cookies = mvcResult.response.cookies
        val refreshCookie = Arrays.stream(cookies)
            .filter { c: Cookie -> c.name == "refreshToken" }
            .findFirst()

        Assertions.assertThat(refreshCookie).isNotEmpty()
        Assertions.assertThat(refreshCookie.get().value).isNotBlank()
        Assertions.assertThat(refreshCookie.get().maxAge).isEqualTo(1209600)
    }
}