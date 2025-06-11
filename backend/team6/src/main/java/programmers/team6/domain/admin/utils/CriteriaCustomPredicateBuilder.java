package programmers.team6.domain.admin.utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.SingularAttribute;
import programmers.team6.domain.admin.enums.Quarter;

/**
 * 필터링을 위한 Predicate 리스트 빌드
 * @author gunwoong
 */
public class CriteriaCustomPredicateBuilder<T> {
	private final CriteriaBuilder cb;
	private final List<Predicate> predicates;

	private CriteriaCustomPredicateBuilder(CriteriaBuilder cb) {
		this.cb = cb;
		this.predicates = new ArrayList<>();
	}

	public static <T> CriteriaCustomPredicateBuilder<T> builder(CriteriaBuilder cb) {
		return new CriteriaCustomPredicateBuilder<>(cb);
	}

	public <R> CriteriaCustomPredicateBuilder<T> applyDateRangeFilter(From<T, R> root,
		SingularAttribute<? super R, LocalDateTime> mappedFromField,
		SingularAttribute<? super R, LocalDateTime> mappedToField,
		LocalDateTime from, LocalDateTime to) {
		if (isProvided(from) && isProvided(to)) {
			this.predicates.add(cb.greaterThanOrEqualTo(root.get(mappedToField), from));
			this.predicates.add(cb.lessThanOrEqualTo(root.get(mappedFromField), to));
		}
		return this;
	}

	public <R> CriteriaCustomPredicateBuilder<T> applyDateRangeFilter(From<T, R> root,
		SingularAttribute<? super R, LocalDateTime> mappedFromField,
		SingularAttribute<? super R, LocalDateTime> mappedToField,
		Integer year, Quarter quarter) {
		if (year == null) {
			return this;
		}
		if (quarter == null) {
			quarter = Quarter.NONE;
		}
		return applyDateRangeFilter(root, mappedFromField, mappedToField, quarter.getStart(year), quarter.getEnd(year));
	}

	public CriteriaCustomPredicateBuilder<T> applyEqualFilter(From<T, ?> root, Object conditionValue,
		SingularAttribute<?, ?>... mappedFields) {
		if (isProvided(conditionValue)) {
			this.predicates.add(cb.equal(CriteriaUtils.searchPath(root, mappedFields), conditionValue));
		}
		return this;
	}

	public CriteriaCustomPredicateBuilder<T> applyNonEqualFilter(From<T, ?> root, Object conditionValue,
		SingularAttribute<?, ?>... mappedFields) {
		if (isProvided(conditionValue)) {
			this.predicates.add(cb.notEqual(CriteriaUtils.searchPath(root, mappedFields), conditionValue));
		}
		return this;
	}

	public CriteriaCustomPredicateBuilder<T> applyLikeFilter(From<T, ?> root, String conditionValue,
		SingularAttribute<?, ?>... mappedFields) {
		if (isProvided(conditionValue)) {
			this.predicates.add(cb.like(CriteriaUtils.searchPath(root, mappedFields), "%" + conditionValue + "%"));
		}
		return this;
	}

	public List<Predicate> build() {
		return predicates;
	}

	private boolean isProvided(Object value) {
		return value != null;
	}
}
