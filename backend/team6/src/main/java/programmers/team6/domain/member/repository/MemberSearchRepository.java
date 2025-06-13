package programmers.team6.domain.member.repository;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder;
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder;
import programmers.team6.global.querybuilder.QueryUtils;
import programmers.team6.domain.member.entity.Dept_;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.Member_;
import programmers.team6.domain.member.enums.Role;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final EntityManager entityManager;

	public Page<Member> searchFrom(String name, Long deptId, Pageable pageable) {
		TypedQuery<Member> query = createSearchQueryFrom(name, deptId, pageable);
		long count = createSearCountFrom(name);
		return QueryUtils.makeQueryToPageable(query, pageable, count);
	}

	public Page<Member> searchFrom(Long deptId, String name, List<Long> ids, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Member> criteriaQuery = criteriaBuilder.createQuery(Member.class);
		Root<Member> from = criteriaQuery.from(Member.class);

		CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(from.get(Member_.id));
		for (Long id : ids) {
			inClause.value(id);
		}

		List<Predicate> predicates = CriteriaCustomPredicateBuilder.<Member>builder(criteriaBuilder)
			.applyLikeFilter(from, name, Member_.name)
			.applyEqualFilter(from, deptId, Member_.dept, Dept_.id)
			.build();

		// in 절 포함해서 전체 조건 생성
		predicates.add(inClause); // inClause가 별도로 존재한다고 가정

		criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
		TypedQuery<Member> query = CriteriaCustomQueryBuilder.builder(
				criteriaQuery, criteriaBuilder)
			.orderBy(from, pageable.getSort())
			.createQuery(entityManager)
			.build();
		return QueryUtils.makeQueryToPageable(query, pageable, countSearchFrom(name, ids));
	}

	private long countSearchFrom(String name, List<Long> ids) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<Member> from = criteriaQuery.from(Member.class);

		criteriaQuery.select(criteriaBuilder.count(from));
		CriteriaBuilder.In<Long> inClause = criteriaBuilder.in(from.get(Member_.id));
		for (Long id : ids) {
			inClause.value(id);
		}

		List<Predicate> predicates = CriteriaCustomPredicateBuilder.<Member>builder(criteriaBuilder)
			.applyLikeFilter(from, name, Member_.name)
			.build();

		criteriaQuery.where(inClause);
		TypedQuery<Long> query = CriteriaCustomQueryBuilder.builder(criteriaQuery, criteriaBuilder)
			.applyDynamicPredicates(predicates)
			.createQuery(entityManager)
			.build();

		return query.getSingleResult();
	}

	private TypedQuery<Member> createSearchQueryFrom(String name, Long deptId, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Member> criteriaQuery = criteriaBuilder.createQuery(Member.class);
		Root<Member> from = criteriaQuery.from(Member.class);

		List<Predicate> predicates = CriteriaCustomPredicateBuilder.<Member>builder(criteriaBuilder)
			.applyLikeFilter(from, name, Member_.name)
			.applyEqualFilter(from, Role.USER, Member_.role)
			.applyEqualFilter(from, deptId, Member_.dept, Dept_.id)
			.build();

		return CriteriaCustomQueryBuilder.builder(criteriaQuery, criteriaBuilder)
			.applyDynamicPredicates(predicates)
			.orderBy(from, pageable.getSort())
			.createQuery(entityManager)
			.build();
	}

	private long createSearCountFrom(String name) {
		return count(rootBuilder -> rootBuilder.from(Member.class),
			(predicatesBuilder, from) ->
				predicatesBuilder.applyLikeFilter(from, name, Member_.name)
					.applyNonEqualFilter(from, Role.PENDING, Member_.role));
	}

	private <T> long count(Function<CriteriaQuery<Long>, Root<T>> rootBuilder,
		BiConsumer<CriteriaCustomPredicateBuilder<T>, Root<T>> predicatesSetter) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<T> from = rootBuilder.apply(criteriaQuery);

		criteriaQuery.select(criteriaBuilder.count(from));

		CriteriaCustomPredicateBuilder<T> builder = CriteriaCustomPredicateBuilder.builder(criteriaBuilder);
		predicatesSetter.accept(builder, from);

		TypedQuery<Long> query = CriteriaCustomQueryBuilder.builder(criteriaQuery, criteriaBuilder)
			.applyDynamicPredicates(builder.build())
			.createQuery(entityManager)
			.build();

		return query.getSingleResult();
	}
}
