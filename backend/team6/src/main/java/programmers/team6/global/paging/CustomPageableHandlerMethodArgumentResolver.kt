package programmers.team6.global.paging

import org.springframework.core.MethodParameter
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableArgumentResolver
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer

private val DEFAULT_PAGEABLE_FACTORY = PageableFactory()
private val DEFAULT_PAGEABLE_VALIDATOR_FACTORY = PageableValidatorFactory()
private val ANNOTATION_TYPE = PagingConfig::class.java

class CustomPageableHandlerMethodArgumentResolver (
    private val pageableFactory: PageableFactory = DEFAULT_PAGEABLE_FACTORY,
    private val pageableValidatorFactory: PageableValidatorFactory = DEFAULT_PAGEABLE_VALIDATOR_FACTORY
) : PageableArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.parameterType == Pageable::class.java &&
                parameter.hasParameterAnnotation(ANNOTATION_TYPE)

    override fun resolveArgument(
        methodParameter: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory
    ): Pageable {
        val config = methodParameter.getMergedAnnotation()
        val pageable = pageableFactory.createPageable(config, webRequest)
        val validator = pageableValidatorFactory.create(config)
        validator.valid(pageable)
        return pageable
    }
}

private fun MethodParameter.getMergedAnnotation(): MergedAnnotation<PagingConfig> =
    MergedAnnotations.from(*this.parameterAnnotations)[ANNOTATION_TYPE]

