package programmers.team6.domain.vacation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import programmers.team6.domain.vacation.entity.VacationInfo;

public interface VacationInfoRepository extends JpaRepository<VacationInfo, Integer> {

	@Query("""
		    SELECT (vi.totalCount - vi.useCount - COALESCE(
		        (SELECT SUM(CASE WHEN vr.type.code = '05' THEN 0.5 ELSE DATEDIFF(vr.to, vr.from) + 1 END) 
		         FROM VacationRequest vr 
		         WHERE vr.member.id = :memberId 
		         AND vr.type.code = :vacationType 
		         AND vr.status = 'IN_PROGRESS'), 0))
		    FROM VacationInfo vi
		    WHERE vi.memberId = :memberId 
		    AND vi.vacationType = :vacationType
		""")
	Optional<Double> findActualRemainingVacationDays(Long memberId, String vacationType);

	@Query("""
			SELECT (vi.totalCount - vi.useCount - COALESCE(
				(SELECT SUM(CASE WHEN vr.type.code = '05' THEN 0.5 ELSE DATEDIFF(vr.to, vr.from) + 1 END) 
				 FROM VacationRequest vr 
				 WHERE vr.member.id = :memberId 
				 AND vr.type.code = :vacationType 
				 AND vr.status = 'IN_PROGRESS'
				 AND vr.id != :excludeRequestId), 0))
			FROM VacationInfo vi
			WHERE vi.memberId = :memberId 
			AND vi.vacationType = :vacationType
		""")
	Optional<Double> findActualRemainingVacationDaysExcludeRequestId(Long memberId, String vacationType,
		Long excludeRequestId);

	Optional<VacationInfo> findByMemberIdAndVacationType(Long memberId, String vacationType);

	@Query("SELECT vi "
		+ "FROM VacationInfo vi "
		+ "JOIN Member m ON vi.memberId = m.id "
		+ "WHERE (FUNCTION('date', m.joinDate) > :startJoinDate "
		+ "AND FUNCTION('day', m.joinDate) = FUNCTION('day', :currentDate)) "
		+ "   OR (FUNCTION('date', m.joinDate) > :startJoinDate "
		+ "AND FUNCTION('day', m.joinDate) > FUNCTION('day', FUNCTION('last_day', :currentDate)) "
		+ "       AND FUNCTION('day', :currentDate) = FUNCTION('day', FUNCTION('last_day', :currentDate))) ")
	List<VacationInfo> findMonthlyVacationFrom(@Param("startJoinDate") LocalDate startJoinDate,
		@Param("currentDate") LocalDate currentDate);

	@Query("SELECT vi "
		+ "FROM VacationInfo vi "
		+ "JOIN Member m ON vi.memberId = m.id "
		+ "WHERE (FUNCTION('date', m.joinDate) <= :startJoinDate "
		+ "AND FUNCTION('day', m.joinDate) = FUNCTION('day', :currentDate) "
		+ "AND FUNCTION('month', m.joinDate) = FUNCTION('month', :currentDate))")
	List<VacationInfo> findAnnualVacationFrom(@Param("startJoinDate") LocalDate startJoinDate,
		@Param("currentDate") LocalDate currentDate);

	List<VacationInfo> findAllByVacationIdIn(List<Integer> ids);

	@Query("SELECT vi "
		+ "FROM VacationInfo vi "
		+ "JOIN Member m ON vi.memberId = m.id "
		+ "WHERE FUNCTION('date', m.joinDate) in :joinDates and vi.vacationType = :type ")
	List<VacationInfo> findAnnualVacationByJoinDates(String type, List<LocalDate> joinDates);

	@Query("SELECT vi "
		+ "FROM VacationInfo vi "
		+ "WHERE FUNCTION('date', vi.updatedAt) in :baseLineDates and vi.vacationType = :type ")
	List<VacationInfo> findByTypeAndCreatedAtToDate(String type, List<LocalDate> baseLineDates);

	List<VacationInfo> findByMemberIdIn(List<Long> memberIds);
}
