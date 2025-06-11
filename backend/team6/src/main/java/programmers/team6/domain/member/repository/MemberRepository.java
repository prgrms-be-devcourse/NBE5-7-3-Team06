package programmers.team6.domain.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import programmers.team6.domain.admin.dto.MemberApprovalResponse;
import programmers.team6.domain.member.dto.MemberLoginInfoResponse;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;

public interface MemberRepository extends JpaRepository<Member, Long> {

	@Query("""
		select new programmers.team6.domain.admin.dto.MemberApprovalResponse(
			m.id, m.name, m.position.name, m.dept.deptName, m.memberInfo.birth, m.memberInfo.email
		)
		from Member m
		where m.role = :role
		""")
	List<MemberApprovalResponse> findPendingMembers(Role role);

	@Query("select m from Member m join fetch m.memberInfo mi where mi.email = :email")
	Optional<Member> findByEmail(@Param("email") String email);

	@Query("SELECT m FROM Member m " +
		"JOIN FETCH m.dept d " +
		"LEFT JOIN FETCH d.deptLeader " +
		"WHERE m.id = :memberId")
	Optional<Member> findByIdWithDeptAndLeader(@Param("memberId") Long memberId);

	@Query("""
		    SELECT new programmers.team6.domain.member.dto.MemberLoginInfoResponse(
		        m.id,
				m.name,
				m.dept.id,
				m.dept.deptName,
				m.position.id,
				m.position.name  
		    )
		    FROM Member m
		    WHERE m.id = :memberId
		""")
	MemberLoginInfoResponse findLoginMemberInfo(Long memberId);

	@Query("""
		select m 
		from Member m
		where m.id in (select vil.memberId from VacationInfoLog vil where vil.logDate < :localDateTime and vil.vacationType = :code group by vil.memberId)
		""")
	Page<Member> findAllHasVacationInfoTargetYear(LocalDateTime localDateTime, String code, Pageable pageable);

	@Query("""
		select m 
		from Member m
		where m.id in (select vil.memberId from VacationInfoLog vil where vil.logDate < :localDateTime and vil.vacationType = :code group by vil.memberId)
		and m.name like %:name%
		""")
	Page<Member> findAllHasVacationInfoTargetYear(LocalDateTime localDateTime, String code, String name,
		Pageable pageable);

}