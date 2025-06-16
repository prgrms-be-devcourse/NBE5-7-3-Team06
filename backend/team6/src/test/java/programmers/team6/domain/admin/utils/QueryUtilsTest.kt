package programmers.team6.domain.admin.utils

import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.TypedQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import programmers.team6.global.querybuilder.QueryUtils.makeQueryToPageable

internal class QueryUtilsTest {
    @Test
    fun should_makeQueryToPageable() {
        // given
        val query: TypedQuery<String> = mockk<TypedQuery<String>>()

        val pageable: Pageable = PageRequest.of(1, 2) // 2개씩, 2페이지(0-based)
        val resultList = mutableListOf<String?>("A", "B", "C", "D", "E")
        val totalCount = 5L

        // when
        every { query.setFirstResult(2) }.returns(query)
        every { query.setMaxResults(2) }.returns(query)
        every {  query.getResultList()}.returns(resultList.subList(2, 4))
        val pages = makeQueryToPageable(query, pageable, totalCount)

        // then
        assertThat(pages).containsExactly("C", "D")
    }
}