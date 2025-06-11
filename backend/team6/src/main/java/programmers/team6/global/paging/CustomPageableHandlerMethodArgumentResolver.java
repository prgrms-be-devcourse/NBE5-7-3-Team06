package programmers.team6.global.paging;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomPageableHandlerMethodArgumentResolver implements PageableArgumentResolver {

	private static final PageableFactory DEFAULT_PAGEABLE_FACTORY = new PageableFactory();
	private static final PageableValidatorFactory DEFAULT_PAGEABLE_VALIDATOR_FACTORY = new PageableValidatorFactory();
	private static final Class<PagingConfig> ANNOTATION_TYPE = PagingConfig.class;

	private final PageableFactory pageableFactory;
	private final PageableValidatorFactory pageableValidatorFactory;

	public CustomPageableHandlerMethodArgumentResolver(PageableFactory pageableFactory,
		PageableValidatorFactory pageableValidatorFactory) {
		this.pageableFactory = pageableFactory;
		this.pageableValidatorFactory = pageableValidatorFactory;
	}

	public CustomPageableHandlerMethodArgumentResolver() {
		this(DEFAULT_PAGEABLE_FACTORY, DEFAULT_PAGEABLE_VALIDATOR_FACTORY);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return isSupport(parameter);
	}

	private boolean isSupport(MethodParameter parameter) {
		return Pageable.class.equals(parameter.getParameterType()) && parameter.hasParameterAnnotation(ANNOTATION_TYPE);
	}

	@Override
	public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		MergedAnnotation<PagingConfig> config = getAnnotation(methodParameter);
		Pageable pageable = pageableFactory.createPageable(config, webRequest);
		PageableValidator validator = pageableValidatorFactory.create(config);
		validator.valid(pageable);
		return pageable;
	}

	private static MergedAnnotation<PagingConfig> getAnnotation(MethodParameter methodParameter) {
		return MergedAnnotations.from(methodParameter.getParameterAnnotations()).get(ANNOTATION_TYPE);
	}
}
