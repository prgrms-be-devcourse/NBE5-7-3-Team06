package programmers.team6.domain.vacation.repository

import org.springframework.data.jpa.repository.JpaRepository
import programmers.team6.domain.vacation.entity.VacationInfo
import java.util.*

interface VacationRepository : JpaRepository<VacationInfo?, Int?> {
    fun findByMemberIdAndVacationType(memberId: Long, vacationType: String): VacationInfo?
}