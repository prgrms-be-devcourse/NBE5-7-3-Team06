package programmers.team6.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;
import programmers.team6.domain.member.util.LoginMemberArgumentResolver;
import programmers.team6.global.paging.CustomPageableHandlerMethodArgumentResolver;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final LoginMemberArgumentResolver loginMemberArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new CustomPageableHandlerMethodArgumentResolver());
		resolvers.add(loginMemberArgumentResolver);
	}

}
