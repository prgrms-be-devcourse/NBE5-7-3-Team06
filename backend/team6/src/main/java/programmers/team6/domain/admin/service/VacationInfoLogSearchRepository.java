package programmers.team6.domain.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.admin.utils.CriteriaCustomPredicateBuilder;
import programmers.team6.domain.admin.utils.CriteriaCustomQueryBuilder;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.entity.VacationInfoLog_;

@Repository
@RequiredArgsConstructor
public class VacationInfoLogSearchRepository {

	private final EntityManager entityManager;

	public List<Long> queryContainVacationInfoMemberIds(LocalDateTime localDateTime, String code) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<VacationInfoLog> from = criteriaQuery.from(VacationInfoLog.class);

		List<Predicate> predicates = CriteriaCustomPredicateBuilder.<VacationInfoLog>builder(criteriaBuilder)
			.applyEqualFilter(from, code, VacationInfoLog_.vacationType)
			.build();
		predicates.add(criteriaBuilder.lessThanOrEqualTo(from.get(VacationInfoLog_.logDate), localDateTime));

		TypedQuery<Long> build = CriteriaCustomQueryBuilder.builder(criteriaQuery, criteriaBuilder)
			.applyDynamicPredicates(predicates)
			.projection(Long.class, from.get(VacationInfoLog_.memberId))
			.createQuery(entityManager)
			.build();
		return  build.getResultList();
	}
}
