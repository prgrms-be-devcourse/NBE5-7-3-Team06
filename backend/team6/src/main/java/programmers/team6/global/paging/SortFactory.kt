package programmers.team6.global.paging

import org.springframework.core.annotation.MergedAnnotation
import org.springframework.data.domain.Sort
import org.springframework.web.context.request.NativeWebRequest

const val SORT_PARAMETER_NAME: String = "sort"

class SortFactory {
    fun create(methodParameter: MergedAnnotation<PagingConfig>, webRequest: NativeWebRequest): Sort {
        val defaultDirection = getDefaultDirection(methodParameter)
        val webSort = getWebSort(defaultDirection, webRequest)
        return if (webSort.isSorted) webSort else getConfigSort(methodParameter, defaultDirection)
    }

    private fun getDefaultDirection(methodParameter: MergedAnnotation<PagingConfig>): Sort.Direction {
        return methodParameter.getEnum("direction", Sort.Direction::class.java)
    }

    private fun getWebSort(defaultDirection: Sort.Direction, webRequest: NativeWebRequest): Sort {
        val sorts = webRequest.getParameterValues(SORT_PARAMETER_NAME) ?: return Sort.unsorted()
        return Sort.by(toOrders(defaultDirection, sorts))
    }

    private fun toOrders(defaultDirection: Sort.Direction, sorts: Array<String>): List<Sort.Order> {
        return sorts.map { toOrder(it, defaultDirection) }
    }

    private fun toOrder(sort: String, defaultDirection: Sort.Direction): Sort.Order {
        val values = sort.split(",", limit = 2)
        require(values.isNotEmpty()) { "입력이 잘 못 되었습니다." }
        return if (values.size == 1) {
            Sort.Order(defaultDirection, values[0])
        } else {
            Sort.Order(Sort.Direction.fromString(values[1]), values[0])
        }
    }

    private fun getConfigSort(methodParameter: MergedAnnotation<PagingConfig>, defaultDirection: Sort.Direction): Sort {
        val sorts = methodParameter.getStringArray(SORT_PARAMETER_NAME)
        return if (sorts.isNullOrEmpty()) {
            Sort.unsorted()
        } else {
            Sort.by(defaultDirection, *sorts)
        }
    }
}
