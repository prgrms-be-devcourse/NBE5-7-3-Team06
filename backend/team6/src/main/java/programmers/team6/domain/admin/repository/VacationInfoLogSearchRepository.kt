package programmers.team6.domain.admin.repository

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.entity.VacationInfoLog_
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder
import java.time.LocalDateTime

@Repository
class VacationInfoLogSearchRepository(private val entityManager: EntityManager) {

    fun queryContainVacationInfoMemberIds(localDateTime: LocalDateTime, code: String?): List<Long> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(Long::class.java)
        val from = criteriaQuery.from(VacationInfoLog::class.java)

        val predicates = CriteriaCustomPredicateBuilder.builder<VacationInfoLog>(criteriaBuilder)
            .applyEqualFilter(from, code, VacationInfoLog_.vacationType)
            .build()
        predicates.add(criteriaBuilder.lessThanOrEqualTo(from.get(VacationInfoLog_.logDate), localDateTime))

        val build = CriteriaCustomQueryBuilder.builder(criteriaQuery, criteriaBuilder)
            .applyDynamicPredicates(predicates)
            .projection(Long::class.java, from.get(VacationInfoLog_.memberId))
            .createQuery(entityManager)
            .build()
        return build.resultList
    }
}
