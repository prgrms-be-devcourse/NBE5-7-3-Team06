package programmers.team6.global.paging

import org.springframework.core.annotation.MergedAnnotation
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.web.context.request.NativeWebRequest

private val DEFAULT_SORT_FACTORY = SortFactory()
private const val PAGE_PARAM_NAME = "page"
private const val PAGE_SIZE_PARAM_NAME = "size"

class PageableFactory (private val sortFactory: SortFactory = DEFAULT_SORT_FACTORY) {
    fun createPageable(config: MergedAnnotation<PagingConfig>, webRequest: NativeWebRequest): Pageable {
        val page = getPage(config, webRequest)
        val size = getSize(config, webRequest)
        val sort = sortFactory.create(config, webRequest)
        return PageRequest.of(page, size, sort)
    }

    private fun getPage(config: MergedAnnotation<PagingConfig>, webRequest: NativeWebRequest): Int {
        return webRequest.getParameter(PAGE_PARAM_NAME)?.toIntOrNull()
            ?: config.getInt(PAGE_PARAM_NAME)
    }

    private fun getSize(config: MergedAnnotation<PagingConfig>, webRequest: NativeWebRequest): Int {
        return webRequest.getParameter(PAGE_SIZE_PARAM_NAME)?.toIntOrNull()
            ?: config.getInt(PAGE_SIZE_PARAM_NAME)
    }
}
