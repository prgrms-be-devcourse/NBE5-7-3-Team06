package programmers.team6.domain.vacation.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import programmers.team6.domain.vacation.entity.VacationInfoLog
import java.time.LocalDateTime

interface VacationInfoLogRepository : JpaRepository<VacationInfoLog?, Long?> {
    @Query(
        """
		select vl
		from VacationInfoLog vl
		where vl.memberId in :ids and vl.vacationType = :code and vl.logDate <= :localDate
		"""
    )
    fun findLastedByMemberIdInAndYear(ids: List<Long>, localDate: LocalDateTime, code: String): List<VacationInfoLog>
}
