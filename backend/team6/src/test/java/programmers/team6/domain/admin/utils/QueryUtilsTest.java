package programmers.team6.domain.admin.utils;

import jakarta.persistence.TypedQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;


class QueryUtilsTest {
    @Test
    void should_makeQueryToPageable() {
        TypedQuery<String> query = Mockito.mock(TypedQuery.class);
        Pageable pageable = PageRequest.of(1, 2); // 2개씩, 2페이지(0-based)
        List<String> resultList = Arrays.asList("A", "B", "C", "D", "E");
        long totalCount = 5L;

        when(query.setFirstResult(2)).thenReturn(query);
        when(query.setMaxResults(2)).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList.subList(2, 4));
        Page<String> pages = QueryUtils.makeQueryToPageable(query, pageable, totalCount);

        Assertions.assertThat(pages).containsExactly("C", "D");
    }

}