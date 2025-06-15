package programmers.team6.domain.auth.service;

import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.auth.dto.JwtMemberInfo;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.auth.dto.TokenPairWithExpiration;
import programmers.team6.domain.auth.dto.request.MemberLoginRequest;
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.auth.dto.response.AccessTokenResponse;
import programmers.team6.domain.auth.dto.response.AuthTokenResponse;
import programmers.team6.domain.auth.dto.response.LoginResponse;
import programmers.team6.domain.auth.token.JwtTokenProvider;
import programmers.team6.domain.auth.util.JwtUtils;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.admin.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberInfoRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.member.util.mapper.MemberMapper;
import programmers.team6.global.exception.code.ConflictErrorCode;
import programmers.team6.global.exception.code.ForbiddenErrorCode;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.exception.customException.ConflictException;
import programmers.team6.global.exception.customException.ForbiddenException;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.global.exception.customException.UnauthorizedException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final MemberInfoRepository memberInfoRepository;
	private final DeptRepository deptRepository;
	private final CodeRepository codeRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtService jwtService;

	public void signUp(MemberSignUpRequest memberSignUpRequest) {

		Dept dept = deptRepository.findById(memberSignUpRequest.dept()).orElseThrow(
			() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT)
		);

		Code position = codeRepository.findByGroupCodeAndCode("POSITION", memberSignUpRequest.position());

		if (isExistsByEmail(memberSignUpRequest.email())) {
			throw new ConflictException(ConflictErrorCode.CONFLICT_EMAIL);
		}

		String encodedPassword = passwordEncoder.encode(memberSignUpRequest.password());

		Member member = MemberMapper.MemberCreateRequestToEntity(
			memberSignUpRequest, dept, position, encodedPassword
		);

		memberRepository.save(member);
	}

	public boolean isExistsByEmail(String email) {
		return memberInfoRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public LoginResponse login(MemberLoginRequest memberLoginRequest) {

		Member member = memberRepository.findByEmail(memberLoginRequest.email())
			.orElseThrow(() -> new NotFoundException(NotFoundErrorCode.NOT_FOUND_EMAIL));

		if (member.getRole().equals(Role.PENDING)) {
			throw new ForbiddenException(ForbiddenErrorCode.FORBIDDEN_PENDING);
		}

		if (!passwordEncoder.matches(memberLoginRequest.password(), member.getMemberInfo().getPassword())) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_PASSWORD);
		}

		TokenPairWithExpiration tokenPair = jwtTokenProvider.generateTokenPair(
			new JwtMemberInfo(member.getId(), member.getName(), member.getRole()));

		AuthTokenResponse authTokenResponse = new AuthTokenResponse(tokenPair.accessToken(),
			tokenPair.accessTokenExpiresIn(), member.getId(),
			member.getName(), member.getRole());

		return new LoginResponse(authTokenResponse, tokenPair.refreshToken(), tokenPair.refreshTokenExpiresIn());
	}

	public AccessTokenResponse reissue(String refreshToken) {

		jwtTokenProvider.validate(refreshToken);

		jwtTokenProvider.validateNotBlackListed(refreshToken);

		return jwtTokenProvider.generateAccessToken(refreshToken);
	}

	public void addBlackList(String refreshToken) {

		TokenBody tokenBody = jwtTokenProvider.parseClaims(refreshToken);
		Date expiration = tokenBody.expiration();

		jwtService.addBlackList(refreshToken, JwtUtils.calculateTtlMillis(expiration));
	}

}
