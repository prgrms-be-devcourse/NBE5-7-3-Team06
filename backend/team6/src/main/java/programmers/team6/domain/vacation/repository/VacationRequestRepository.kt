package programmers.team6.domain.vacation.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.entity.VacationRequest
import java.time.LocalDateTime

interface VacationRequestRepository : JpaRepository<VacationRequest, Long> {
    @Query(
        """
			select count(vr.id)
			from VacationRequest vr
			where vr.member.id = :memberId and (vr.status = 'APPROVED' or vr.status = 'IN_PROGRESS')
			and ((vr.from <= :from and :from <= vr.to) or (vr.from <= :to and :to <= vr.to) or (:from <= vr.from and vr.to <= :to)) 
		"""
    )
    fun countInRangeFromBetweenToBy(memberId: Long, from: LocalDateTime, to: LocalDateTime): Long

    @Query(
        """
		   select count(vr.id)
		   from VacationRequest vr
		   where vr.member.id = :memberId and (vr.status = 'APPROVED' or vr.status = 'IN_PROGRESS')
		   and vr.id != :excludeRequestId
		   and ((vr.from <= :from and :from <= vr.to) or (vr.from <= :to and :to <= vr.to) or (:from <= vr.from and vr.to <= :to)) 
		"""
    )
    fun countInRangeFromBetweenToByExcludeRequestId(
        memberId: Long, from: LocalDateTime, to: LocalDateTime,
        excludeRequestId: Long
    ): Long

    @Query(
        """
			select case
				when :typeCode = '05' then 0.5
				else (DATEDIFF(:to, :from) + 1)
			end
		"""
    )
    fun calculateRequestedVacationDays(from: LocalDateTime, to: LocalDateTime, typeCode: String): Double

    @Query("SELECT vr.id FROM VacationRequest vr WHERE vr.member.id = :memberId ORDER BY vr.createdAt DESC")
    fun findIdsByRequesterIdPaging(@Param("memberId") memberId: Long, pageable: Pageable): Page<Long>

    @Query("SELECT vr FROM VacationRequest vr JOIN FETCH vr.type JOIN FETCH vr.member WHERE vr.id IN :ids ORDER BY vr.createdAt DESC")
    fun findByIdsWithFetch(@Param("ids") ids: MutableList<Long>): MutableList<VacationRequest>

    @Query(
        value = ("select new programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse(vr.id,vr.from, vr.to, m.id ,m.name, d.deptName,p.name,vr.reason,t.name,vr.status) "
                + "from VacationRequest vr join vr.type t " + "join vr.member m join m.dept d join m.position p "
                + "where vr.id = :id")
    )
    fun findVacationRequestDetailById(@Param("id") id: Long): VacationRequestDetailReadResponse?

    fun findVacationRequestById(id: Long): VacationRequest?

    @Query(
        """
		select vr 
		from VacationRequest vr 
		where (vr.type.code in :codes)
				and vr.status = 'APPROVED' 
				and vr.member.id in :ids 
				and (year(vr.from) = :year or year(vr.to) = :year)
		"""
    )
    fun findByMemberIdInAndYear(
        ids: List<Long>,
        year: Int,
        codes: List<String>
    ): List<VacationRequest>
}
