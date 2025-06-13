package programmers.team6.global.querybuilder;

import java.util.List;

import org.springframework.data.domain.Sort;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * 주어진 필터(Predicates)를 기반으로 쿼리 생성 및 projection
 * @param <T>
 * @author gunwoong
 */
public class CriteriaCustomQueryBuilder<T> {
	private CriteriaQuery<T> cq;
	private CriteriaBuilder cb;
	private TypedQuery<T> typedQuery;

	private CriteriaCustomQueryBuilder(CriteriaQuery<T> cq, CriteriaBuilder cb) {
		this.cq = cq;
		this.cb = cb;
	}

	public static <T> CriteriaCustomQueryBuilder<T> builder(CriteriaQuery<T> cq, CriteriaBuilder cb) {
		return new CriteriaCustomQueryBuilder<>(cq, cb);
	}

	public CriteriaCustomQueryBuilder<T> applyDynamicPredicates(List<Predicate> predicates) {
		cq.where(cb.and(predicates.toArray(new Predicate[0])));
		return this;
	}

	public CriteriaCustomQueryBuilder<T> projection(Class<T> projectionClass, Expression... projectionFields) {
		cq.select(cb.construct(projectionClass, projectionFields));
		return this;
	}

	public CriteriaCustomQueryBuilder<T> groupBy(Expression... groupFields) {
		cq.groupBy(groupFields);
		return this;
	}

	public <X> CriteriaCustomQueryBuilder<T> orderByLatest(From<?, ?> root, SingularAttribute<X, ?> mappedField) {
		cq.orderBy(cb.desc(CriteriaUtils.searchPath(root, mappedField)));
		return this;
	}

	public CriteriaCustomQueryBuilder<T> createQuery(EntityManager entityManager) {
		this.typedQuery = entityManager.createQuery(cq);
		return this;
	}

	public TypedQuery<T> build() {
		return typedQuery;
	}

	public CriteriaCustomQueryBuilder<T> orderBy(From<?, ?> root, Sort sort) {
		List<Order> orders = toOrders(root, sort);
		cq.orderBy(orders);
		return this;
	}

	private List<Order> toOrders(From<?, ?> root, Sort sort) {
		return sort.stream().map(order -> toOrder(root, order)).toList();
	}

	private Order toOrder(From<?, ?> root, Sort.Order order) {
		if (order.isAscending()) {
			return cb.asc(root.get(order.getProperty()));
		}
		return cb.desc(root.get(order.getProperty()));
	}
}
