package programmers.team6.global.querybuilder

import jakarta.persistence.TypedQuery
import lombok.experimental.UtilityClass
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

object QueryUtils {
    /**
     * 제공된 쿼리를 페이징하여 페이지 객체 리턴
     */
    @JvmStatic
    fun <T> makeQueryToPageable(query: TypedQuery<T>, pageable: Pageable, totalCount: Long): Page<T> {
        return PageImpl<T>(
            query.setFirstResult(pageable.getOffset().toInt())
                .setMaxResults(pageable.getPageSize())
                .getResultList(), pageable, totalCount
        )
    }
}
