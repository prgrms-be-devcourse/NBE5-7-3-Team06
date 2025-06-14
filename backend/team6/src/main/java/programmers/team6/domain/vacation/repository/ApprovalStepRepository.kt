package programmers.team6.domain.vacation.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import java.time.LocalDateTime

interface ApprovalStepRepository : JpaRepository<ApprovalStep, Long> {
    // 휴가 요청에 대한 첫 번째 결재 단계 조회 (단계 오름차순)
    fun findFirstByVacationRequestOrderByStepAsc(vacationRequest: VacationRequest): ApprovalStep?

    @Query("SELECT a FROM ApprovalStep a JOIN FETCH a.member WHERE a.vacationRequest.id IN :requestIds AND a.step = 1")
    fun findFirstStepsByVacationRequestIds(@Param("requestIds") requestIds: List<Long>): List<ApprovalStep>

    @Query(
        value = ("select new programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse(m.name,asp.reason,asp.approvalStatus) from ApprovalStep asp "
                + "join VacationRequest vr on asp.vacationRequest=vr join asp.member m "
                + "where vr.id = :vacationId order by asp.step")
    )
    fun findApprovalStepDetailById(@Param("vacationId") vacationId: Long): List<ApprovalStepDetailUpdateResponse>

    fun findApprovalStepsByVacationRequest_IdOrderByStepAsc(vacationRequestId: Long): List<ApprovalStep>


    @Query(
        """
		select new programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse(
					a.id, vr.type.name, vr.from, vr.to, vr.member.name,
					vr.member.dept.deptName, vr.member.position.name, a.approvalStatus
				)
		from ApprovalStep a join a.member m join a.vacationRequest vr
		where a.member.id = :memberId and a.step = :step
		order by a.createdAt desc
		"""
    )
    fun findFirstStepByMemberId(
        memberId: Long,
        step: Int,
        pageable: Pageable
    ): Page<ApprovalFirstStepSelectResponse>

    @Query(
        """
		select new programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse(
					a.id, vr.type.name, vr.from, vr.to, vr.member.name,
					vr.member.dept.deptName, vr.member.position.name, a.approvalStatus
				)
		from ApprovalStep a join a.member m join a.vacationRequest vr
		where a.member.id = :memberId and a.step = :step
				and (:type is null or vr.type.name = :type)
				and (:name is null or vr.member.name like concat('%', :name, '%'))
				and (:from is null or :to is null or (vr.from <= :to and  vr.to >= :from))
				and (:status is null or a.approvalStatus = :status)
		order by a.createdAt desc
		
		"""
    )
    fun findFirstStepByFilter(
        memberId: Long, type: String?, name: String?,
        from: LocalDateTime?, to: LocalDateTime?, status: ApprovalStatus?, step: Int, pageable: Pageable
    ): Page<ApprovalFirstStepSelectResponse>

    @Query(
        """
		select new programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse(
					a2.id, vr.type.name, vr.from, vr.to, vr.member.name,
					vr.member.dept.deptName, vr.member.position.name, a1.approvalStatus, a2.approvalStatus
				)
		from ApprovalStep a2 join a2.vacationRequest vr
				join ApprovalStep a1 on a1.vacationRequest.id = vr.id and a1.step = 1
		where a2.member.id = :memberId and a2.step = :step
		order by a2.createdAt desc
		
		"""
    )
    fun findSecondStepByMemberId(
        memberId: Long,
        step: Int,
        pageable: Pageable
    ): Page<ApprovalSecondStepSelectResponse>

    @Query(
        """
		select new programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse(
					a2.id, vr.type.name, vr.from, vr.to, vr.member.name,
					vr.member.dept.deptName, vr.member.position.name, a1.approvalStatus, a2.approvalStatus
				)
		from ApprovalStep a2 join a2.vacationRequest vr
				join ApprovalStep a1 on a1.vacationRequest.id = vr.id and a1.step = 1
		where a2.member.id = :memberId and a2.step = :step
                and (:type is null or vr.type.name = :type)
				and (:name is null or vr.member.name like concat('%', :name, '%'))
				and (:from is null or :to is null or (vr.from <= :to and  vr.to >= :from))
				and (:status is null or a2.approvalStatus = :status)
		order by a2.createdAt desc
		
		"""
    )
    fun findSecondStepByFilter(
        memberId: Long, type: String?, name: String?,
        from: LocalDateTime?, to: LocalDateTime?, status: ApprovalStatus?, step: Int, pageable: Pageable
    ): Page<ApprovalSecondStepSelectResponse>

    @Query(
        """
			select a
			from ApprovalStep a
			join fetch a.member am
			join fetch a.vacationRequest vr
			join fetch vr.type t
			join fetch vr.member vrm
			join fetch vrm.dept d
			join fetch vrm.position p
			where a.id = :id and am.id = :memberId and a.step = :step
		
		"""
    )
    fun findByIdAndMemberIdAndStep(id: Long, memberId: Long, step: Int): ApprovalStep?

    fun findByVacationRequestAndStep(vacationRequest: VacationRequest, step: Int): ApprovalStep?

    fun findByVacationRequest(vacationRequest: VacationRequest): List<ApprovalStep>
}
