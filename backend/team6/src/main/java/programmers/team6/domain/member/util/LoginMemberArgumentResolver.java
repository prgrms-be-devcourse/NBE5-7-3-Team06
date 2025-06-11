package programmers.team6.domain.member.util;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.auth.dto.TokenBody;
import programmers.team6.domain.auth.token.JwtTokenProvider;
import programmers.team6.domain.member.annotation.LoginMember;
import programmers.team6.domain.member.dto.MemberLoginInfoResponse;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.code.UnauthorizedErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.global.exception.customException.UnauthorizedException;

@Component
@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		return parameter.hasParameterAnnotation(LoginMember.class)
			   && parameter.getParameterType().equals(MemberLoginInfoResponse.class);
	}

	@Override
	public MemberLoginInfoResponse resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,

		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest nativeRequest = (HttpServletRequest)webRequest.getNativeRequest();

		String accessToken = jwtTokenProvider.extractToken(nativeRequest);

		if (accessToken == null) {
			throw new UnauthorizedException(UnauthorizedErrorCode.UNAUTHORIZED_INVALID_HEADER);
		}

		TokenBody tokenBody = jwtTokenProvider.parseClaims(accessToken);

		Long id = tokenBody.id();

		MemberLoginInfoResponse loginMemberInfo = memberRepository.findLoginMemberInfo(id);

		if (loginMemberInfo == null) {
			throw new NotFoundException(NotFoundErrorCode.NOT_FOUND_MEMBER);
		}

		return loginMemberInfo;
	}
}
