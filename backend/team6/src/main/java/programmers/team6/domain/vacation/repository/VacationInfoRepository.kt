package programmers.team6.domain.vacation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import programmers.team6.domain.vacation.entity.VacationInfo
import java.time.LocalDate
import java.util.*

interface VacationInfoRepository : JpaRepository<VacationInfo, Int> {
    @Query(
        """
		    SELECT (vi.totalCount - vi.useCount - COALESCE(
		        (SELECT SUM(CASE WHEN vr.type.code = '05' THEN 0.5 ELSE DATEDIFF(vr.to, vr.from) + 1 END) 
		         FROM VacationRequest vr 
		         WHERE vr.member.id = :memberId 
		         AND vr.type.code = :vacationType 
		         AND vr.status = 'IN_PROGRESS'), 0))
		    FROM VacationInfo vi
		    WHERE vi.memberId = :memberId 
		    AND vi.vacationType = :vacationType
		
		"""
    )
    fun findActualRemainingVacationDays(memberId: Long, vacationType: String): Optional<Double>

    @Query(
        """
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
		
		"""
    )
    fun findActualRemainingVacationDaysExcludeRequestId(
        memberId: Long?, vacationType: String?,
        excludeRequestId: Long?
    ): Optional<Double?>?

    fun findByMemberIdAndVacationType(memberId: Long, vacationType: String): Optional<VacationInfo>

    @Query(
        ("SELECT vi "
                + "FROM VacationInfo vi "
                + "JOIN Member m ON vi.memberId = m.id "
                + "WHERE (FUNCTION('date', m.joinDate) > :startJoinDate "
                + "AND FUNCTION('day', m.joinDate) = FUNCTION('day', :currentDate)) "
                + "   OR (FUNCTION('date', m.joinDate) > :startJoinDate "
                + "AND FUNCTION('day', m.joinDate) > FUNCTION('day', FUNCTION('last_day', :currentDate)) "
                + "       AND FUNCTION('day', :currentDate) = FUNCTION('day', FUNCTION('last_day', :currentDate))) ")
    )
    fun findMonthlyVacationFrom(
        @Param("startJoinDate") startJoinDate: LocalDate,
        @Param("currentDate") currentDate: LocalDate
    ): List<VacationInfo>

    @Query(
        ("SELECT vi "
                + "FROM VacationInfo vi "
                + "JOIN Member m ON vi.memberId = m.id "
                + "WHERE (FUNCTION('date', m.joinDate) <= :startJoinDate "
                + "AND FUNCTION('day', m.joinDate) = FUNCTION('day', :currentDate) "
                + "AND FUNCTION('month', m.joinDate) = FUNCTION('month', :currentDate))")
    )
    fun findAnnualVacationFrom(
        @Param("startJoinDate") startJoinDate: LocalDate?,
        @Param("currentDate") currentDate: LocalDate?
    ): List<VacationInfo?>?

    fun findAllByVacationIdIn(ids: List<Int>): List<VacationInfo>

    @Query(
        ("SELECT vi "
                + "FROM VacationInfo vi "
                + "JOIN Member m ON vi.memberId = m.id "
                + "WHERE FUNCTION('date', m.joinDate) in :joinDates and vi.vacationType = :type ")
    )
    fun findAnnualVacationByJoinDates(type: String, joinDates: List<LocalDate>): List<VacationInfo>

    @Query(
        ("SELECT vi "
                + "FROM VacationInfo vi "
                + "WHERE FUNCTION('date', vi.updatedAt) in :baseLineDates and vi.vacationType = :type ")
    )
    fun findByTypeAndCreatedAtToDate(type: String, baseLineDates: List<LocalDate>): List<VacationInfo>

    fun findByMemberIdIn(memberIds: List<Long>): List<VacationInfo>
}
