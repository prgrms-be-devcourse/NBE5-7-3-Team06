package programmers.team6.domain.vacation.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Predicate
import org.springframework.stereotype.Repository
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder.Companion.builder
import java.time.LocalDateTime

@Repository
class VacationRequestSearchRepository(
    private val em: EntityManager
) {

    fun findApprovedVacationsByMonth(
        status: VacationRequestStatus?, start: LocalDateTime?, end: LocalDateTime?, deptId: Long?
    ): List<VacationRequestCalendarResponse> {
        val cb = em.getCriteriaBuilder()
        val cq = cb.createQuery(VacationRequestCalendarResponse::class.java)

        val vr = cq.from(VacationRequest::class.java)
        val m = vr.join<VacationRequest, Member>("member")
        val d = m.join<Member, Dept>("dept")
        val p = m.join<Member, Code>("position")
        val type = vr.join<VacationRequest, Code>("type")

        val predicates: MutableList<Predicate> = mutableListOf()

        predicates.add(cb.equal(vr.get<Any?>("status"), status))
        predicates.add(cb.greaterThanOrEqualTo<LocalDateTime?>(vr.get<LocalDateTime?>("from"), start))
        predicates.add(cb.lessThan<LocalDateTime?>(vr.get<LocalDateTime?>("to"), end))

        if (deptId != 0L) {
            predicates.add(cb.equal(d.get<Any?>("id"), deptId))
        }

        return builder<VacationRequestCalendarResponse>(cq, cb)
            .applyDynamicPredicates(predicates.toList())
            .projection(
                VacationRequestCalendarResponse::class.java,
                m.get<Any?>("name"),
                d.get<Any?>("deptName"),
                type.get<Any?>("name"),
                p.get<Any?>("name"),
                vr.get<Any?>("from"),
                vr.get<Any?>("to")
            )
            .createQuery(em)
            .build()
            .getResultList()
    }
}
