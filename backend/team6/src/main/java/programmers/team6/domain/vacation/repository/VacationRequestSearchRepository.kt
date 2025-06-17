package programmers.team6.domain.vacation.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.*
import org.springframework.stereotype.Repository
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder
import java.time.LocalDateTime

@Repository
class VacationRequestSearchRepository(private val em: EntityManager) {

    fun findApprovedVacationsByMonth(
        status: VacationRequestStatus,
        start: LocalDateTime,
        end: LocalDateTime,
        deptId: Long?
    ): List<VacationRequestCalendarResponse> {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(VacationRequestCalendarResponse::class.java)

        val vr = cq.from(VacationRequest::class.java)
        val m = vr.join<VacationRequest, Member>("member")
        val d = m.join<Member, Dept>("dept")
        val p = m.join<Member, Code>("position")
        val type = vr.join<VacationRequest, Code>("type")

        val predicates = mutableListOf<Predicate>().apply {
            add(cb.equal(vr.get<VacationRequestStatus>("status"), status))
            add(cb.greaterThanOrEqualTo(vr.get("from"), start))
            add(cb.lessThan(vr.get("to"), end))

            if (deptId != 0L) {
                add(cb.equal(d.get<Long>("id"), deptId))
            }
        }

        return CriteriaCustomQueryBuilder.builder(cq, cb)
            .applyDynamicPredicates(predicates)
            .projection(
                VacationRequestCalendarResponse::class.java,
                m.get<String>("name"),
                d.get<String>("deptName"),
                type.get<String>("name"),
                p.get<String>("name"),
                vr.get<LocalDateTime>("from"),
                vr.get<LocalDateTime>("to")
            )
            .createQuery(em)
            .build()
            .resultList
    }
}