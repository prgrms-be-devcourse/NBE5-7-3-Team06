package programmers.team6.domain.vacation.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

@Repository
@RequiredArgsConstructor
public class VacationRequestSearchRepository {

	private final EntityManager em;

	public List<VacationRequestCalendarResponse> findApprovedVacationsByMonth(
		VacationRequestStatus status, LocalDateTime start, LocalDateTime end, Long deptId
	) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<VacationRequestCalendarResponse> cq = cb.createQuery(VacationRequestCalendarResponse.class);

		Root<VacationRequest> vr = cq.from(VacationRequest.class);
		Join<VacationRequest, Member> m = vr.join("member");
		Join<Member, Dept> d = m.join("dept");
		Join<Member, Code> p = m.join("position");
		Join<VacationRequest, Code> type = vr.join("type");

		List<Predicate> predicates = new ArrayList<>();

		predicates.add(cb.equal(vr.get("status"), status));
		predicates.add(cb.greaterThanOrEqualTo(vr.get("from"), start));
		predicates.add(cb.lessThan(vr.get("to"), end));

		if (deptId != 0) {
			predicates.add(cb.equal(d.get("id"), deptId));
		}

		return CriteriaCustomQueryBuilder.builder(cq, cb)
			.applyDynamicPredicates(predicates)
			.projection(
				VacationRequestCalendarResponse.class,
				m.get("name"),
				d.get("deptName"),
				type.get("name"),
				p.get("name"),
				vr.get("from"),
				vr.get("to")
			)
			.createQuery(em)
			.build()
			.getResultList();
	}
}
