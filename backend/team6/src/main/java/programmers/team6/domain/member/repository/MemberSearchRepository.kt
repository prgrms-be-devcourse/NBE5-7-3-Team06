package programmers.team6.domain.member.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.persistence.metamodel.SingularAttribute
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import programmers.team6.domain.admin.entity.Dept_
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.Member_
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder
import programmers.team6.global.querybuilder.QueryUtils

@Repository
@RequiredArgsConstructor
class MemberSearchRepository(private val entityManager: EntityManager) {

    fun searchFrom(name: String? = null, deptId: Long? = null, pageable: Pageable): Page<Member> {
        val query = createSearchQueryFrom(name, deptId, pageable)
        val count = createSearchCountFrom(name)
        return QueryUtils.makeQueryToPageable(query, pageable, count)
    }

    fun searchFrom(deptId: Long?, name: String?, ids: List<Long>, pageable: Pageable): Page<Member> {
        val query = createMemberQuery(name, deptId, ids, pageable)
        val count = countSearchFrom(name, ids)
        return QueryUtils.makeQueryToPageable(query, pageable, count)
    }

    private fun createSearchQueryFrom(name: String?, deptId: Long?, pageable: Pageable): TypedQuery<Member> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Member::class.java)
        val from = cq.from(Member::class.java)

        val predicates = CriteriaCustomPredicateBuilder.builder<Member>(cb).apply {
            applyLikeFilter(from, name, Member_.name)
            applyEqualFilter(from, Role.USER, Member_.role)
            applyEqualFilter(from, deptId, Member_.dept, Dept_.id)
        }.build()

        return CriteriaCustomQueryBuilder.builder(cq, cb)
            .applyDynamicPredicates(predicates)
            .orderBy(from, pageable.sort)
            .createQuery(entityManager)
            .build()
    }

    private fun createSearchCountFrom(name: String?): Long {
        return countQuery<Member> { cb, root ->
            CriteriaCustomPredicateBuilder.builder<Member>(cb)
                .applyLikeFilter(root, name, Member_.name)
                .applyNonEqualFilter(root, Role.PENDING, Member_.role)
                .build()
        }
    }

    private fun createMemberQuery(
        name: String?,
        deptId: Long?,
        ids: List<Long>?,
        pageable: Pageable
    ): TypedQuery<Member> {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Member::class.java)
        val from = cq.from(Member::class.java)

        val predicates = CriteriaCustomPredicateBuilder.builder<Member>(cb).apply {
            applyLikeFilter(from, name, Member_.name)
            applyEqualFilter(from, deptId, Member_.dept, Dept_.id)
            if (!ids.isNullOrEmpty()) {
                val inClause = cb.buildInClause(from, Member_.id, ids)
                build().add(inClause)
            }
        }.build()

        cq.where(cb.and(*predicates.toTypedArray()))
        return CriteriaCustomQueryBuilder.builder(cq, cb)
            .orderBy(from, pageable.sort)
            .createQuery(entityManager)
            .build()
    }

    private fun countSearchFrom(name: String?, ids: List<Long>): Long {
        return countQuery<Member> { cb, root ->
            val base = CriteriaCustomPredicateBuilder.builder<Member>(cb)
                .applyLikeFilter(root, name, Member_.name)
                .build()
            val inClause = cb.buildInClause(root, Member_.id, ids)
            base + inClause
        }
    }

    private inline fun <reified T> countQuery(
        crossinline predicateBuilder: (CriteriaBuilder, Root<T>) -> List<Predicate>
    ): Long {
        val cb = entityManager.criteriaBuilder
        val cq = cb.createQuery(Long::class.java)
        val root = cq.from(T::class.java)
        cq.select(cb.count(root))

        val predicates = predicateBuilder(cb, root)
        cq.where(cb.and(*predicates.toTypedArray()))

        return entityManager.createQuery(cq).singleResult
    }

    private fun <T> CriteriaBuilder.buildInClause(
        root: Root<T>,
        path: SingularAttribute<T, Long>,
        values: List<Long>
    ): Predicate = this.`in`(root[path]).apply { values.forEach(::value) }
}
