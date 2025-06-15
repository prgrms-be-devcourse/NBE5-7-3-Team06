package programmers.team6.domain.admin.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition
import programmers.team6.domain.admin.dto.response.VacationRequestSearchResponse
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Code_
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.entity.Dept_
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.Member_
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.ApprovalStep_
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.entity.VacationRequest_
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.entity.BaseEntity_
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder.Companion.builder
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder.Companion.builder
import programmers.team6.global.querybuilder.QueryUtils
import java.time.LocalDateTime

@Repository
@RequiredArgsConstructor
class AdminVacationRequestSearchCustom(
    private val entityManager: EntityManager
) {

    /**
     * ApprovalStep와 VacationRequest를 join하고 AdminVacationSearchCondition의 변수들을 통해 다중 필터 구현
     * From<A></A>,B> = 'from 엔티티'에서 엔티티 부분 , A타입의 객체부터 B타입의 속성을 탐색 (그래서 Root<A></A>,A>이고 Join<A></A>,B>임, Root와 Join 둘다 From 상속)
     * SingularAttribute<C></C>,D> = 메타모델에서 사용하는 단일 속성 타입 정보 , C = 특정 엔티티, D = C 엔티티의 필드 타입
     * Path<X> = 메타모델 경로 혹은 루트로부터 탐색된 속성(특정 속성의 경로) , X = 속성 타입, 해당 path가 가르키는 최종 필드 타입
     * @param searchCondition 검색 필터
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
    </X> */
    fun search(
        searchCondition: AdminVacationSearchCondition,
        pageable: Pageable
    ): Page<VacationRequestSearchResponse> {
        val cb = entityManager.getCriteriaBuilder()
        val cq = cb.createQuery(VacationRequestSearchResponse::class.java)

        val `as` = cq.from(ApprovalStep::class.java)
        val vr = `as`.join<ApprovalStep, VacationRequest>("vacationRequest")

        /** 필터링
         * 1. 휴가 신청 범위
         * 2. 특정 년도 혹은 특정 분기 (1,2,3,4,상,하반기)
         * 3. 휴가 신청자 이름
         * 4. 부서 이름
         * 5. 휴가 종류
         * 6. 휴가 신청자 포지션
         * 7. 휴가 신청 상태
         */
        val predicates = makeSearchQuery(cb, vr, searchCondition)

        /**
         * Predicates들을 기반을 Query 생성
         */
        val query = builder<VacationRequestSearchResponse>(cq, cb)
            .applyDynamicPredicates(predicates)
            .projection(
                VacationRequestSearchResponse::class.java,
                vr.get<Long?>(VacationRequest_.id),
                vr.get<Code?>(VacationRequest_.type).get<String?>(Code_.name),
                vr.get<LocalDateTime?>(VacationRequest_.from),
                vr.get<LocalDateTime?>(VacationRequest_.to),
                vr.get<Member?>(VacationRequest_.member).get<String?>(Member_.name),
                cb.function<String?>(
                    "GROUP_CONCAT",
                    String::class.java,
                    `as`.get<Member?>(ApprovalStep_.member).get<String?>(Member_.name)
                ),
                vr.get<Member?>(VacationRequest_.member).get<Dept?>(Member_.dept).get<String?>(Dept_.deptName),
                vr.get<VacationRequestStatus?>(VacationRequest_.status)
            )
            .groupBy(vr.get<Long?>(VacationRequest_.id))
            .orderByLatest<LocalDateTime>(vr, BaseEntity_.createdAt)
            .createQuery(entityManager)
            .build()

        val countQuery = cb.createQuery<Long?>(Long::class.java)
        val countRoot = countQuery.from<ApprovalStep?>(ApprovalStep::class.java)
        val countVr = countRoot.join<ApprovalStep?, VacationRequest?>("vacationRequest", JoinType.INNER)

        // count 쿼리용 predicate를 새로 생성
        val countPredicates = makeSearchQuery(cb, countVr, searchCondition)

        // GROUP BY가 있는 경우 DISTINCT COUNT를 사용
        countQuery.select(cb.countDistinct(countVr.get<Long?>(VacationRequest_.id)))
            .where(*countPredicates.toTypedArray<Predicate?>())

        val totalCount = entityManager.createQuery<Long>(countQuery).getSingleResult()

        return QueryUtils.makeQueryToPageable<VacationRequestSearchResponse>(query, pageable, totalCount)
    }

    private fun makeSearchQuery(
        cb: CriteriaBuilder, vr: Join<ApprovalStep, VacationRequest>,
        searchCondition: AdminVacationSearchCondition
    ): List<Predicate> {
        return builder<ApprovalStep>(cb)
            .applyDateRangeFilter<VacationRequest>(
                vr, VacationRequest_.from, VacationRequest_.to,
                searchCondition.dateRange.start, searchCondition.dateRange.end
            )
            .applyDateRangeFilter<VacationRequest>(
                vr, VacationRequest_.from, VacationRequest_.to,
                searchCondition.dateRange.year, searchCondition.dateRange.quarter
            )
            .applyLikeFilter(vr, searchCondition.applicant.name, VacationRequest_.member, Member_.name)
            .applyLikeFilter(
                vr, searchCondition.applicant.deptName, VacationRequest_.member, Member_.dept,
                Dept_.deptName
            )
            .applyEqualFilter(
                vr, searchCondition.applicant.vacationTypeCodeId, VacationRequest_.type,
                Code_.id
            )
            .applyEqualFilter(
                vr, searchCondition.applicant.positionCodeId, VacationRequest_.member,
                Member_.position, Code_.id
            )
            .applyEqualFilter(vr, searchCondition.vacationRequestStatus, VacationRequest_.status)
            .build()
    }
}
