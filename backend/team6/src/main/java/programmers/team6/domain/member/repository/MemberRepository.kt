package programmers.team6.domain.member.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import programmers.team6.domain.admin.dto.response.MemberApprovalResponse
import programmers.team6.domain.member.dto.response.MemberLoginInfoResponse
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import java.time.LocalDateTime
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {
    @Query(
        """
		select new programmers.team6.domain.admin.dto.response.MemberApprovalResponse(
			m.id, m.name, m.position.name, m.dept.deptName, m.memberInfo.birth, m.memberInfo.email
		)
		from Member m
		where m._role = :role
		
		"""
    )
    fun findPendingMembers(role: Role?): List<MemberApprovalResponse>

    @Query("select m from Member m join fetch m.memberInfo mi where mi.email = :email")
    fun findByEmail(@Param("email") email: String): Member?

    @Query(
        ("SELECT m FROM Member m " +
                "JOIN FETCH m.dept d " +
                "LEFT JOIN FETCH d.deptLeader " +
                "WHERE m.id = :memberId")
    )
    fun findByIdWithDeptAndLeader(@Param("memberId") memberId: Long): Optional<Member>

    @Query(
        """
		    SELECT new programmers.team6.domain.member.dto.response.MemberLoginInfoResponse(
		        m.id,
				m.name,
				m.dept.id,
				m.dept.deptName,
				m.position.id,
				m.position.name  
		    )
		    FROM Member m
		    WHERE m.id = :memberId
		
		"""
    )
    fun findLoginMemberInfo(memberId: Long?): MemberLoginInfoResponse?

    @Query(
        """
		select m 
		from Member m
		where m.id in (select vil.memberId from VacationInfoLog vil where vil.logDate < :localDateTime and vil.vacationType = :code group by vil.memberId)
		
		"""
    )
    fun findAllHasVacationInfoTargetYear(
        localDateTime: LocalDateTime,
        code: String,
        pageable: Pageable
    ): Page<Member>

    @Query(
        """
		select m 
		from Member m
		where m.id in (select vil.memberId from VacationInfoLog vil where vil.logDate < :localDateTime and vil.vacationType = :code group by vil.memberId)
		and m.name like %:name%
		
		"""
    )
    fun findAllHasVacationInfoTargetYear(
        localDateTime: LocalDateTime, code: String, name: String,
        pageable: Pageable
    ): Page<Member>
}