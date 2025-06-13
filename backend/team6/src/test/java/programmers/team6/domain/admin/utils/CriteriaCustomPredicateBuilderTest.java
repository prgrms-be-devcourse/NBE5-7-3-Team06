package programmers.team6.domain.admin.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import programmers.team6.domain.admin.enums.Quarter;
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriteriaCustomPredicateBuilderTest {
    private CriteriaBuilder cb;
    private From<Object, Object> root;
    private CriteriaCustomPredicateBuilder customPredicateBuilder;
    private Predicate predicate = null;

    @BeforeEach
    void setUp() {
        this.cb = mock(CriteriaBuilder.class);
        this.root = mock(From.class);
        this.customPredicateBuilder = CriteriaCustomPredicateBuilder.builder(cb);
    }

    @Nested
    @DisplayName("Date Range Filter 적용시, ")
    class should_applyDateRangeFilter {

        @Test
        @DisplayName("시작일과 종료일이 모두 유효할 때, 필터 적용")
        void success_whenValidFromAndTo() {
            // given
            LocalDateTime from = LocalDateTime.now().minusDays(1);
            LocalDateTime to = LocalDateTime.now();

            // when
            when(cb.greaterThanOrEqualTo(nullable(Path.class), any(LocalDateTime.class))).thenReturn(predicate);
            when(cb.lessThanOrEqualTo(nullable(Path.class), any(LocalDateTime.class))).thenReturn(predicate);

            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilder = customPredicateBuilder.applyDateRangeFilter(root, null, null, from, to);

            // then
            assertThat(criteriaCustomPredicateBuilder.build()).hasSize(2);
        }

        @ParameterizedTest
        @DisplayName("시작일과 종료일이 유효하지 않을 때, 필터 무시")
        @MethodSource("invalidFromAndToProvider")
        void fail_whenInvalidFromAndTo(LocalDateTime from, LocalDateTime to) {
            // when
            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilder = customPredicateBuilder.applyDateRangeFilter(root, null, null, from, to);

            // then
            assertThat(criteriaCustomPredicateBuilder.build()).isEmpty();
        }

        static Stream<Arguments> invalidFromAndToProvider() {
            return Stream.of(Arguments.of(null, null), Arguments.of(LocalDateTime.now().minusDays(1), null), Arguments.of(null, LocalDateTime.now()));
        }

        @ParameterizedTest
        @DisplayName("년도와 분기가 유효할 때, 필터 적용")
        @MethodSource("validYearAndQuarterProvider")
        void success_whenValidYearAndQuarter(Integer year, Quarter quarter) {
            // when
            when(cb.greaterThanOrEqualTo(nullable(Path.class), any(LocalDateTime.class))).thenReturn(predicate);
            when(cb.lessThanOrEqualTo(nullable(Path.class), any(LocalDateTime.class))).thenReturn(predicate);

            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilder = customPredicateBuilder.applyDateRangeFilter(root, null, null, year, quarter);

            // then
            assertThat(criteriaCustomPredicateBuilder.build()).hasSize(2);
        }

        static Stream<Arguments> validYearAndQuarterProvider() {
            return Stream.of(Arguments.of(2023, null), Arguments.of(2023, Quarter.NONE), Arguments.of(2023, Quarter.Q1), Arguments.of(2023, Quarter.Q2), Arguments.of(2023, Quarter.H1), Arguments.of(2023, Quarter.H2));
        }

        @ParameterizedTest
        @DisplayName("년도와 분기가 유효하지 않을 때, 필터 무시")
        @MethodSource("invalidYearAndQuarterProvider")
        void fail_whenInvalidYearAndQuarter(Integer year, Quarter quarter) {
            // when
            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilder = customPredicateBuilder.applyDateRangeFilter(root, null, null, year, quarter);

            // then
            assertThat(criteriaCustomPredicateBuilder.build()).isEmpty();
        }

        static Stream<Arguments> invalidYearAndQuarterProvider() {
            return Stream.of(Arguments.of(null, null), Arguments.of(null, Quarter.Q1), Arguments.of(null, Quarter.Q2), Arguments.of(null, Quarter.H1), Arguments.of(null, Quarter.H2));
        }
    }

    @Nested
    @DisplayName("Equal Filter / Non-Equal Filter 적용시, ")
    class should_applyEqualFilter {

        @Test
        @DisplayName("조건 값이 유효할 때, 필터 적용")
        void success_whenConditionValueIsValid() {
            // given
            Path<Object> path = mock(Path.class);
            SingularAttribute<Object, Object> field = mock(SingularAttribute.class);
            Object value = "test";

            // when
            when(root.get(field)).thenReturn(path);
            when(cb.equal(any(), eq(value))).thenReturn(predicate);
            when(cb.notEqual(any(), eq(value))).thenReturn(predicate);

            CriteriaCustomPredicateBuilder<Object> criteriaCustomPredicateBuilderAppliedEqualFilter = CriteriaCustomPredicateBuilder.builder(cb).applyEqualFilter(root, value, field);
            CriteriaCustomPredicateBuilder<Object> criteriaCustomPredicateBuilderAppliedNonEqualFilter = CriteriaCustomPredicateBuilder.builder(cb).applyNonEqualFilter(root, value, field);

            assertThat(criteriaCustomPredicateBuilderAppliedEqualFilter.build()).hasSize(1);
            assertThat(criteriaCustomPredicateBuilderAppliedNonEqualFilter.build()).hasSize(1);
        }

        @Test
        @DisplayName("조건 값이 유효하지 않을 때, 필터 무시")
        void fail_whenConditionValueIsInvalid() {
            // when
            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilderAppliedEqualFilter = customPredicateBuilder.applyEqualFilter(root, null, null);
            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilderAppliedNonEqualFilter = customPredicateBuilder.applyNonEqualFilter(root, null, null);

            // then
            assertThat(criteriaCustomPredicateBuilderAppliedEqualFilter.build()).isEmpty();
            assertThat(criteriaCustomPredicateBuilderAppliedNonEqualFilter.build()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Non-Equal Filter 적용시, ")
    class should_applyNonEqualFilter {

        @Test
        @DisplayName("조건 값이 유효할 때, 필터 적용")
        void success_whenConditionValueIsValid() {
            // given
            Path<Object> path = mock(Path.class);
            SingularAttribute<Object, Object> field = mock(SingularAttribute.class);
            Object value = "test";

            // when
            when(root.get(field)).thenReturn(path);


            CriteriaCustomPredicateBuilder<Object> criteriaCustomPredicateBuilder = CriteriaCustomPredicateBuilder.builder(cb).applyNonEqualFilter(root, value, field);

            assertThat(criteriaCustomPredicateBuilder.build()).hasSize(1);
        }

        @Test
        @DisplayName("조건 값이 유효하지 않을 때, 필터 무시")
        void fail_whenConditionValueIsInvalid() {
            // when
            CriteriaCustomPredicateBuilder criteriaCustomPredicateBuilder = customPredicateBuilder.applyNonEqualFilter(root, null, null);

            // then
            assertThat(criteriaCustomPredicateBuilder.build()).isEmpty();
        }
    }

}