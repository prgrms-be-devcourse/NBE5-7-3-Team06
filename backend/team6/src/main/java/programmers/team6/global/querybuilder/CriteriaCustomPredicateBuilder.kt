package programmers.team6.global.querybuilder

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.From
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.metamodel.SingularAttribute
import programmers.team6.domain.admin.enums.Quarter
import java.time.LocalDateTime

/**
 * 필터링을 위한 Predicate 리스트 빌드
 * @author gunwoong
 */
class CriteriaCustomPredicateBuilder<T> private constructor(
    private val cb: CriteriaBuilder,
    private val predicates: MutableList<Predicate> = mutableListOf()
) {

    fun <R> applyDateRangeFilter(
        root: From<T, R>,
        mappedFromField: SingularAttribute<in R, LocalDateTime>,
        mappedToField: SingularAttribute<in R, LocalDateTime>,
        from: LocalDateTime?, to: LocalDateTime?
    ): CriteriaCustomPredicateBuilder<T> {
        if (isProvided(from, to)) {
            this.predicates.add(cb.greaterThanOrEqualTo<LocalDateTime?>(root.get<LocalDateTime?>(mappedToField), from))
            this.predicates.add(cb.lessThanOrEqualTo<LocalDateTime?>(root.get<LocalDateTime?>(mappedFromField), to))
        }
        return this
    }

    fun <R> applyDateRangeFilter(
        root: From<T, R>,
        mappedFromField: SingularAttribute<in R, LocalDateTime>,
        mappedToField: SingularAttribute<in R, LocalDateTime>,
        year: Int?, quarter: Quarter?
    ): CriteriaCustomPredicateBuilder<T> {
        var quarter = quarter ?: Quarter.NONE
        if (isProvided(year)) {
            return applyDateRangeFilter(
                root,
                mappedFromField,
                mappedToField,
                quarter.getStart(year!!),
                quarter.getEnd(year!!)
            )
        }

        return this
    }

    fun applyEqualFilter(
        root: From<T, *>, conditionValue: Any?,
        vararg mappedFields: SingularAttribute<*, *>
    ): CriteriaCustomPredicateBuilder<T> {
        if (isProvided(conditionValue)) {
            this.predicates.add(cb.equal(CriteriaUtils.searchPath(root, *mappedFields), conditionValue))
        }
        return this
    }

    fun applyNonEqualFilter(
        root: From<T, *>, conditionValue: Any?,
        vararg mappedFields: SingularAttribute<*, *>
    ): CriteriaCustomPredicateBuilder<T> {
        if (isProvided(conditionValue)) {
            this.predicates.add(cb.notEqual(CriteriaUtils.searchPath(root, *mappedFields), conditionValue))
        }
        return this
    }

    fun applyLikeFilter(
        root: From<T, *>, conditionValue: String?,
        vararg mappedFields: SingularAttribute<*, *>
    ): CriteriaCustomPredicateBuilder<T> {
        if (isProvided(conditionValue)) {
            this.predicates.add(
                cb.like(
                    CriteriaUtils.searchPath(root, *mappedFields) as Expression<String>,
                    "%" + conditionValue + "%"
                )
            )
        }
        return this
    }

    fun build(): MutableList<Predicate> {
        return predicates
    }

    private fun isProvided(vararg values: Any?): Boolean {
        return values.all { it != null }
    }

    companion object {
        @JvmStatic
        fun <T> builder(cb: CriteriaBuilder): CriteriaCustomPredicateBuilder<T> {
            return CriteriaCustomPredicateBuilder(cb)
        }
    }
}
