package programmers.team6.global.config

import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import programmers.team6.domain.member.util.LoginMemberArgumentResolver
import programmers.team6.global.paging.CustomPageableHandlerMethodArgumentResolver

@Configuration
class WebConfig(
    private val loginMemberArgumentResolver: LoginMemberArgumentResolver
) : WebMvcConfigurer{
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(CustomPageableHandlerMethodArgumentResolver())
        resolvers.add(loginMemberArgumentResolver)
    }
}
