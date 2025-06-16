package programmers.team6.global.paging;

import org.springframework.core.annotation.MergedAnnotation;

public class PageableValidatorFactory {

	private static final String ATTRIBUTE_NAME = "maxSize";

	public PageableValidator create(MergedAnnotation<PagingConfig> configMergedAnnotation) {
		return new PageableValidator(configMergedAnnotation.getInt(ATTRIBUTE_NAME));
	}
}
