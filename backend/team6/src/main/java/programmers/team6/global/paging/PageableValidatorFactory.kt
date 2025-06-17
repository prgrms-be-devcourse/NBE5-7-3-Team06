package programmers.team6.global.paging

import org.springframework.core.annotation.MergedAnnotation

private const val ATTRIBUTE_NAME = "maxSize"

class PageableValidatorFactory {
    fun create(configMergedAnnotation: MergedAnnotation<PagingConfig>): PageableValidator {
        return PageableValidator(configMergedAnnotation.getInt(ATTRIBUTE_NAME))
    }
}
