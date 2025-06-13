package programmers.team6.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.http.Cookie;
import programmers.team6.domain.auth.dto.request.MemberLoginRequest;
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.auth.token.JwtTokenProvider;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.admin.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberInfoRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceTests {

	@Autowired
	private AuthService authService;
	@Autowired
	private DeptRepository deptRepository;
	@Autowired
	private CodeRepository codeRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private MemberInfoRepository memberInfoRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JwtService jwtService;
	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	private ObjectMapper objectMapper(ObjectMapper objectMapper) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	@BeforeEach
	void beforeEach() {
		Dept dept = Dept.builder()
			.deptName("개발팀")
			.build();

		deptRepository.save(dept);

		Code code = Code.builder()
			.name("사원")
			.groupCode("POSITION")
			.code("01")
			.build();

		codeRepository.save(code);

	}

	@Test
	@DisplayName("회원가입 성공 테스트")
	void signUpSuccessTest() throws Exception {
		MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
			"홍길동",
			"hong@naver.com",
			1L,
			"01",
			LocalDateTime.now(),
			"1990-01-01",
			"password1234");

		authService.signUp(memberSignUpRequest);

		Member savedMember = memberRepository.findByEmail("hong@naver.com")
			.orElseThrow(() -> new RuntimeException("회원 저장 실패 "));

		assertThat(savedMember.getName()).isEqualTo(memberSignUpRequest.name());
		assertThat(savedMember.getDept().getId()).isEqualTo(memberSignUpRequest.dept());
		assertThat(savedMember.getPosition().getCode()).isEqualTo(memberSignUpRequest.position());
		assertThat(savedMember.getJoinDate()).isEqualTo(memberSignUpRequest.joinDate());

		MemberInfo savedMemberInfo = memberInfoRepository.findById(savedMember.getId())
			.orElseThrow(() -> new RuntimeException("회원 저장 실패 "));

		assertThat(savedMemberInfo.getBirth()).isEqualTo(memberSignUpRequest.birth());
		assertThat(savedMemberInfo.getEmail()).isEqualTo(memberSignUpRequest.email());
		assertThat(
			passwordEncoder.matches(memberSignUpRequest.password(), savedMemberInfo.getPassword())).isTrue();
	}

	@Nested
	@DisplayName("회원가입 실패 테스트")
	class signUpFailTests {

		@Test
		@DisplayName("not found- dept")
		void NotFoundDeptTest() throws Exception {

			Long invalidDeptId = -99L;

			MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
				"홍길동",
				"hong@naver.com",
				invalidDeptId,
				"01",
				LocalDateTime.now(),
				"1990-01-01",
				"password1234");

			NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> authService.signUp(memberSignUpRequest));

			assertThat(notFoundException.getErrorCode()).isEqualTo(NotFoundErrorCode.NOT_FOUND_DEPT);
		}

		@Test
		@DisplayName("not found- position")
		void NotFoundPositionTest() throws Exception {

			String invalidPosition = "invalidPosition";

			MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
				"홍길동",
				"hong@naver.com",
				1L,
				invalidPosition,
				LocalDateTime.now(),
				"1990-01-01",
				"password1234");

			NotFoundException notFoundException = assertThrows(NotFoundException.class,
				() -> authService.signUp(memberSignUpRequest));

			assertThat(notFoundException.getErrorCode()).isEqualTo(NotFoundErrorCode.NOT_FOUND_POSITION);
		}

	}

	@Test
	@DisplayName("회원가입_로그인_권한_api 접근_로그아웃_통합테스트")
	void loginLogoutTest() throws Exception {

		ObjectMapper objectMapper = objectMapper(new ObjectMapper());

		//1. 회원가입
		MemberSignUpRequest memberSignUpRequest = new MemberSignUpRequest(
			"홍길동",
			"hong@naver.com",
			1L,
			"01",
			LocalDateTime.now(),
			"1990-01-01",
			"password1234");

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(memberSignUpRequest)))
			.andExpect(status().isCreated());

		//2. 로그인 요청
		MemberLoginRequest memberLoginRequest = new MemberLoginRequest("hong@naver.com", "password1234");
		MvcResult loginResult = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(memberLoginRequest)))
			.andExpect(status().isOk())
			.andReturn();

		String response = loginResult.getResponse().getContentAsString();
		JsonNode jsonNode = objectMapper.readTree(response);
		String jwtToken = jsonNode.get("token").get("accessToken").asText();
		Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");

		//3. 인증 확인
		// mockMvc.perform(get("/members")
		// 		.header("Authorization", "Bearer " + jwtToken))
		// 	.andExpect(status().isOk());

		//4. 로그아웃
		mockMvc.perform(post("/auth/logout")
				.header("Authorization", "Bearer " + jwtToken)
				.cookie(refreshCookie))
			.andExpect(status().isOk());

		//5. 로그아웃 후 다시 접근
		// mockMvc.perform(post("/members")
		// 		.header("Authorization", "Bearer " + jwtToken))
		// 	.andExpect(status().isUnauthorized());
	}

}