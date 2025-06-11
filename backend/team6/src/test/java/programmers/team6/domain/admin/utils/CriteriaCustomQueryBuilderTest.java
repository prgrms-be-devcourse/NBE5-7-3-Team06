package programmers.team6.domain.admin.utils;

import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CriteriaCustomQueryBuilderTest {
    private CriteriaQuery cq;
    private CriteriaBuilder cb;
    private CriteriaCustomQueryBuilder criteriaCustomQueryBuilder;
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        this.cq = mock(CriteriaQuery.class);
        this.cb = mock(CriteriaBuilder.class);
        this.criteriaCustomQueryBuilder = CriteriaCustomQueryBuilder.builder(cq, cb);
        this.predicate = mock(Predicate.class);
    }

    @Test
    void applyDynamicPredicates() {
        // given
        List<Predicate> predicates = List.of(predicate);

        // when
        when(cb.and(any())).thenReturn(predicate);
        when(cq.where(any(Predicate.class))).thenReturn(cq);
        criteriaCustomQueryBuilder.applyDynamicPredicates(predicates);

        // then
        verify(cq, times(1)).where(any(Predicate.class));
    }

    @Test
    void applyProjection() {
        // given
        CompoundSelection mockSelection = mock(CompoundSelection.class);
        Expression projectionField = mock(Expression.class);

        // when
        when(cb.construct(Object.class, projectionField)).thenReturn(mockSelection);
        when(cq.select(any(CompoundSelection.class))).thenReturn(cq);
        criteriaCustomQueryBuilder.projection(Object.class, projectionField);

        // then
        verify(cq, times(1)).select(any(CompoundSelection.class));
    }

    @Test
    void applyGroupBy() {
        // given
        Expression groupField = mock(Expression.class);

        // when
        when(cq.groupBy(any(Expression.class))).thenReturn(cq);
        criteriaCustomQueryBuilder.groupBy(groupField);

        // then
        verify(cq, times(1)).groupBy(any(Expression.class));
    }

    @Test
    void orderByLatest() {
        // given
        From<?, ?> root = mock(From.class);
        SingularAttribute<?, ?> mappedField = mock(SingularAttribute.class);
        Expression searchPath = mock(Expression.class);
        Path<Object> path = mock(Path.class);

        // when
        when(root.get(any(SingularAttribute.class))).thenReturn(path);
        when(cb.desc(path)).thenReturn(mock(Order.class));
        when(cq.orderBy(any(Order.class))).thenReturn(cq);
        criteriaCustomQueryBuilder.orderByLatest(root, mappedField);

        // then
        verify(cq, times(1)).orderBy(any(Order.class));
    }

    @Test
    void applyOrderBy() {
        // given
        Sort sort = Sort.by(Sort.Order.desc("test1"), Sort.Order.desc("test2"), Sort.Order.asc("test3"));
        From root = mock(From.class);

        // when
        when(cq.orderBy(anyList())).thenReturn(cq);
        criteriaCustomQueryBuilder.orderBy(root, sort);

        // then
        verify(cq, times(1)).orderBy(anyList());
        verify(cb, times(1)).asc(any());
        verify(cb, times(2)).desc(any());
    }
}