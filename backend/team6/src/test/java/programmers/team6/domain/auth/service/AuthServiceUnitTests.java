package programmers.team6.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static programmers.team6.support.PositionMother.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.dto.TokenPairWithExpiration;
import programmers.team6.domain.auth.dto.request.MemberLoginRequest;
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.auth.dto.response.AuthTokenResponse;
import programmers.team6.domain.auth.dto.response.LoginResponse;
import programmers.team6.domain.auth.token.JwtTokenProvider;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberInfoRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.global.exception.code.ConflictErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.exception.customException.ConflictException;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.global.exception.customException.UnauthorizedException;
import programmers.team6.support.MemberMother;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTests {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private MemberInfoRepository memberInfoRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private DeptRepository deptRepository;
	@Mock
	private CodeRepository codeRepository;
	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private AuthService authService;

	@Test
	@DisplayName("회원가입 성공 테스트")
	void signUp_success() {

		String encodedPassword = "encoded1234";

		Dept dept = Dept.builder()
			.deptName("개발팀")
			.build();

		Code position = employee();

		MemberSignUpRequest memberReq = genMemberSignUpRequest();

		when(deptRepository.findById(memberReq.dept())).thenReturn(Optional.of(dept));
		when(codeRepository.findByGroupCodeAndCode("POSITION", memberReq.position())).thenReturn(Optional.of(position));
		when(memberInfoRepository.existsByEmail(memberReq.email())).thenReturn(false);
		when(passwordEncoder.encode(memberReq.password())).thenReturn(encodedPassword);

		authService.signUp(memberReq);

		ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);

		verify(memberRepository).save(captor.capture());

		Member saved = captor.getValue();

		assertThat(saved)
			.extracting("name", "joinDate", "role")
			.containsExactly(memberReq.name(), memberReq.joinDate(), Role.PENDING);

		assertThat(saved.getMemberInfo())
			.extracting("email", "password", "birth")
			.containsExactly(memberReq.email(), encodedPassword, memberReq.birth());

		assertThat(saved.getDept().getDeptName()).isEqualTo(dept.getDeptName());

		assertThat(saved.getPosition().getCode()).isEqualTo(memberReq.position());
	}

	@Test
	@DisplayName("회원가입 시 없는 부서정보가 들어오면 예외를 반환한다.")
	void signUp_dept_exception() {

		MemberSignUpRequest memberReq = genMemberSignUpRequest();

		when(deptRepository.findById(memberReq.dept())).thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> {
				authService.signUp(memberReq);
			}
		).isInstanceOf(NotFoundException.class)
			.hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_DEPT);
	}

	@Test
	@DisplayName("회원가입 시 없는 직위코드가 들어오면 예외를 반환한다.")
	void signUp_position_exception() {

		MemberSignUpRequest memberReq = genMemberSignUpRequest();

		Dept dept = Dept.builder()
			.deptName("개발팀")
			.build();

		when(deptRepository.findById(memberReq.dept())).thenReturn(Optional.of(dept));
		when(codeRepository.findByGroupCodeAndCode("POSITION", memberReq.position())).thenReturn(Optional.empty());

		assertThatThrownBy(
			() -> {
				authService.signUp(memberReq);
			}
		).isInstanceOf(NotFoundException.class)
			.hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_POSITION);
	}

	@Test
	@DisplayName("회원가입 시 이메일 중복이면 예외를 반환한다.")
	void signUp_email_exception() {

		MemberSignUpRequest memberReq = genMemberSignUpRequest();

		Dept dept = Dept.builder()
			.deptName("개발팀")
			.build();

		Code position = employee();

		when(deptRepository.findById(memberReq.dept())).thenReturn(Optional.of(dept));
		when(codeRepository.findByGroupCodeAndCode("POSITION", memberReq.position())).thenReturn(Optional.of(position));
		when(memberInfoRepository.existsByEmail(memberReq.email())).thenReturn(true);

		assertThatThrownBy(
			() -> authService.signUp(memberReq)
		).isInstanceOf(ConflictException.class)
			.hasFieldOrPropertyWithValue("errorCode", ConflictErrorCode.CONFLICT_EMAIL);
	}

	@Test
	@DisplayName("로그인 성공")
	void login_successfully() {
		Member member = MemberMother.withId(1L);
		MemberInfo info = member.getMemberInfo();
		String email = info.getEmail();
		String password = info.getPassword();

		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

		when(passwordEncoder.matches(password, password)).thenReturn(true);

		TokenPairWithExpiration tokenPair = new TokenPairWithExpiration("accessToken", "refreshToken", 200, 1000);
		when(jwtTokenProvider.generateTokenPair(new JwtMemberInfo(1L, member.getName(), member.getRole()))).thenReturn(
			tokenPair);

		LoginResponse response = authService.login(new MemberLoginRequest(email, password));

		AuthTokenResponse authTokenResponse = new AuthTokenResponse(tokenPair.accessToken(),
			tokenPair.accessTokenExpiresIn(), member.getId(),
			member.getName(), member.getRole());
		assertThat(response.authTokenResponse()).isEqualTo(authTokenResponse);
		assertThat(response.refreshToken()).isEqualTo(tokenPair.refreshToken());
		assertThat(response.refreshTokenExpiresIn()).isEqualTo(tokenPair.refreshTokenExpiresIn());
	}

	@Test
	@DisplayName("이메일이 존재하지않는 경우 실패한다")
	void fails_login_when_email_is_not_found() {
		Member member = MemberMother.withId(1L);
		MemberInfo info = member.getMemberInfo();
		String email = info.getEmail();
		String password = info.getPassword();

		when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
		MemberLoginRequest memberLoginRequest = new MemberLoginRequest(email, password);

		assertThatThrownBy(
			() -> authService.login(memberLoginRequest)
		).isInstanceOf(NotFoundException.class)
			.hasFieldOrPropertyWithValue("errorCode", NotFoundErrorCode.NOT_FOUND_EMAIL);

	}

	@Test
	@DisplayName("비밀번호가 일치하지 않으면 테스트가 실패한다")
	void fails_login_when_password_does_not_match() {
		Member member = MemberMother.withId(1L);
		MemberInfo info = member.getMemberInfo();
		String email = info.getEmail();
		String password = info.getPassword();
		String inputPassword = "invalidpassword";
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

		when(passwordEncoder.matches(inputPassword, password)).thenReturn(false);
		MemberLoginRequest memberLoginRequest = new MemberLoginRequest(email, inputPassword);

		assertThatThrownBy(
			() -> authService.login(memberLoginRequest)
		).isInstanceOf(UnauthorizedException.class)
			.hasFieldOrPropertyWithValue("errorCode", UnauthorizedErrorCode.UNAUTHORIZED_PASSWORD);

	}

	private static MemberSignUpRequest genMemberSignUpRequest() {

		return new MemberSignUpRequest(
			"member1",
			"test@test.com",
			1L,
			"01",
			LocalDateTime.of(2024, 1, 1, 12, 0),
			"1989-10-10",
			"qwer1234!"
		);

	}

}
