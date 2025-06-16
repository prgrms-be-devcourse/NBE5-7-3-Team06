package programmers.team6.global.querybuilder

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.*
import jakarta.persistence.metamodel.SingularAttribute
import org.springframework.data.domain.Sort
import programmers.team6.global.querybuilder.CriteriaUtils.searchPath

/**
 * 주어진 필터(Predicates)를 기반으로 쿼리 생성 및 projection
 * @param <T>
 * @author gunwoong
</T> */
class CriteriaCustomQueryBuilder<T> private constructor(
    private val cq: CriteriaQuery<T>,
    private val cb: CriteriaBuilder,
    private var typedQuery: TypedQuery<T>? = null
) {

    fun applyDynamicPredicates(predicates: List<Predicate>): CriteriaCustomQueryBuilder<T> {
        cq.where(cb.and(*predicates.toTypedArray()))
        return this
    }

    fun projection(
        projectionClass: Class<T>,
        vararg projectionFields: Expression<*>
    ): CriteriaCustomQueryBuilder<T> {
        cq.select(cb.construct(projectionClass, *projectionFields))
        return this
    }

    fun groupBy(vararg groupFields: Expression<*>): CriteriaCustomQueryBuilder<T> {
        cq.groupBy(*groupFields)
        return this
    }

    fun <X> orderByLatest(root: From<*, *>, mappedField: SingularAttribute<*,*>): CriteriaCustomQueryBuilder<T> {
        cq.orderBy(cb.desc(searchPath(root, mappedField)))
        return this
    }

    fun createQuery(entityManager: EntityManager): CriteriaCustomQueryBuilder<T> {
        this.typedQuery = entityManager.createQuery(cq)
        return this
    }

    fun build(): TypedQuery<T> {
        return typedQuery!!
    }

    fun orderBy(root: From<*, *>, sort: Sort): CriteriaCustomQueryBuilder<T> {
        val orders = toOrders(root, sort)
        cq.orderBy(orders)
        return this
    }

    private fun toOrders(root: From<*, *>, sort: Sort): List<Order> {
        return sort.stream().map<Order> { order: Sort.Order -> toOrder(root, order) }.toList()
    }

    private fun toOrder(root: From<*, *>, order: Sort.Order): Order {
        if (order.isAscending()) {
            return cb.asc(root.get<Any>(order.getProperty()))
        }
        return cb.desc(root.get<Any>(order.getProperty()))
    }

    companion object {
        @JvmStatic
        fun <T> builder(cq: CriteriaQuery<T>, cb: CriteriaBuilder): CriteriaCustomQueryBuilder<T> {
            return CriteriaCustomQueryBuilder(cq, cb)
        }
    }
}
