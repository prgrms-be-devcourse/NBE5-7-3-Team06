package programmers.team6.domain.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition;
import programmers.team6.domain.admin.dto.response.VacationRequestSearchResponse;
import programmers.team6.domain.admin.entity.Code_;
import programmers.team6.domain.admin.entity.Dept_;
import programmers.team6.global.querybuilder.CriteriaCustomPredicateBuilder;
import programmers.team6.global.querybuilder.CriteriaCustomQueryBuilder;
import programmers.team6.global.querybuilder.QueryUtils;
import programmers.team6.domain.member.entity.Member_;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.ApprovalStep_;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.entity.VacationRequest_;
import programmers.team6.global.entity.BaseEntity_;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminVacationRequestSearchCustom {
	private final EntityManager entityManager;

	/**
	 * ApprovalStep와 VacationRequest를 join하고 AdminVacationSearchCondition의 변수들을 통해 다중 필터 구현
	 * From<A,B> = 'from 엔티티'에서 엔티티 부분 , A타입의 객체부터 B타입의 속성을 탐색 (그래서 Root<A,A>이고 Join<A,B>임, Root와 Join 둘다 From 상속)
	 * SingularAttribute<C,D> = 메타모델에서 사용하는 단일 속성 타입 정보 , C = 특정 엔티티, D = C 엔티티의 필드 타입
	 * Path<X> = 메타모델 경로 혹은 루트로부터 탐색된 속성(특정 속성의 경로) , X = 속성 타입, 해당 path가 가르키는 최종 필드 타입
	 * @param searchCondition 검색 필터
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	public Page<VacationRequestSearchResponse> search(AdminVacationSearchCondition searchCondition, Pageable pageable) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<VacationRequestSearchResponse> cq = cb.createQuery(VacationRequestSearchResponse.class);

		Root<ApprovalStep> as = cq.from(ApprovalStep.class);
		Join<ApprovalStep, VacationRequest> vr = as.join("vacationRequest");

		/** 필터링
		 * 1. 휴가 신청 범위
		 * 2. 특정 년도 혹은 특정 분기 (1,2,3,4,상,하반기)
		 * 3. 휴가 신청자 이름
		 * 4. 부서 이름
		 * 5. 휴가 종류
		 * 6. 휴가 신청자 포지션
		 * 7. 휴가 신청 상태
		 */

		List<Predicate> predicates = CriteriaCustomPredicateBuilder.<ApprovalStep>builder(cb)
			.applyDateRangeFilter(vr, VacationRequest_.from, VacationRequest_.to, searchCondition.dateRange().start(),
				searchCondition.dateRange().end())
			.applyDateRangeFilter(vr, VacationRequest_.from, VacationRequest_.to, searchCondition.dateRange().year(),
				searchCondition.dateRange().quarter())
			.applyLikeFilter(vr, searchCondition.applicant().name(), VacationRequest_.member, Member_.name)
			.applyLikeFilter(vr, searchCondition.applicant().deptName(), VacationRequest_.member, Member_.dept,
				Dept_.deptName)
			.applyEqualFilter(vr, searchCondition.applicant().vacationTypeCodeId(), VacationRequest_.type, Code_.id)
			.applyEqualFilter(vr, searchCondition.applicant().positionCodeId(), VacationRequest_.member,
				Member_.position, Code_.id)
			.applyEqualFilter(vr, searchCondition.vacationRequestStatus(), VacationRequest_.status)
			.build();

		/**
		 * Predicates들을 기반을 Query 생성
		 */
		TypedQuery<VacationRequestSearchResponse> query = CriteriaCustomQueryBuilder.builder(cq, cb)
			.applyDynamicPredicates(predicates)
			.projection(VacationRequestSearchResponse.class, vr.get(VacationRequest_.id),
				vr.get(VacationRequest_.type).get(Code_.name),
				vr.get(VacationRequest_.from), vr.get(VacationRequest_.to),
				vr.get(VacationRequest_.member).get(Member_.name),
				cb.function("GROUP_CONCAT", String.class, as.get(ApprovalStep_.member).get(Member_.name)),
				vr.get(VacationRequest_.member).get(Member_.dept).get(Dept_.deptName), vr.get(VacationRequest_.status))
			.groupBy(vr.get(VacationRequest_.id))
			.orderByLatest(vr, BaseEntity_.createdAt)
			.createQuery(entityManager)
			.build();

		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<ApprovalStep> countRoot = countQuery.from(ApprovalStep.class);
		Join<ApprovalStep, VacationRequest> countVr = countRoot.join("vacationRequest", JoinType.INNER);

		// count 쿼리용 predicate를 새로 생성
		List<Predicate> countPredicates = CriteriaCustomPredicateBuilder.<ApprovalStep>builder(cb)
			.applyDateRangeFilter(countVr, VacationRequest_.from, VacationRequest_.to,
				searchCondition.dateRange().start(),
				searchCondition.dateRange().end())
			.applyDateRangeFilter(countVr, VacationRequest_.from, VacationRequest_.to,
				searchCondition.dateRange().year(),
				searchCondition.dateRange().quarter())
			.applyLikeFilter(countVr, searchCondition.applicant().name(), VacationRequest_.member, Member_.name)
			.applyLikeFilter(countVr, searchCondition.applicant().deptName(), VacationRequest_.member, Member_.dept,
				Dept_.deptName)
			.applyEqualFilter(countVr, searchCondition.applicant().vacationTypeCodeId(), VacationRequest_.type,
				Code_.id)
			.applyEqualFilter(countVr, searchCondition.applicant().positionCodeId(), VacationRequest_.member,
				Member_.position, Code_.id)
			.applyEqualFilter(countVr, searchCondition.vacationRequestStatus(), VacationRequest_.status)
			.build();

		// GROUP BY가 있는 경우 DISTINCT COUNT를 사용
		countQuery.select(cb.countDistinct(countVr.get(VacationRequest_.id)))
			.where(countPredicates.toArray(new Predicate[0]));

		Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

		return QueryUtils.makeQueryToPageable(query, pageable, totalCount);
	}
}
