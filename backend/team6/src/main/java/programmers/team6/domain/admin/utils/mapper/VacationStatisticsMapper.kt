package programmers.team6.domain.admin.utils.mapper

import org.springframework.data.domain.Page
import programmers.team6.domain.admin.support.Members
import programmers.team6.domain.admin.support.VacationInfoLogs
import programmers.team6.domain.admin.support.VacationRequests
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.VacationMonthlyStatisticsResponse

object VacationStatisticsMapper {
    fun toDto(
        members: Members, vacationRequests: VacationRequests,
        logs: VacationInfoLogs, year: Int
    ): Page<VacationMonthlyStatisticsResponse> {
        return members.toPages().map { member: Member ->
            toDto(member, vacationRequests, logs, year)
        }
    }

    private fun toDto(
        member: Member, vacationRequests: VacationRequests,
        logs: VacationInfoLogs, year: Int
    ): VacationMonthlyStatisticsResponse {
        val targeted = vacationRequests.targetRequests(member.id)
        val vacationInfo = logs.findVacationInfo(member.id)
        return VacationMonthlyStatisticsResponse(
            member.id,
            member.name,
            vacationInfo.totalCount,
            vacationInfo.useCount,
            vacationInfo.remainingCount(),
            targeted.count(year, 1),
            targeted.count(year, 2),
            targeted.count(year, 3),
            targeted.count(year, 4),
            targeted.count(year, 5),
            targeted.count(year, 6),
            targeted.count(year, 7),
            targeted.count(year, 8),
            targeted.count(year, 9),
            targeted.count(year, 10),
            targeted.count(year, 11),
            targeted.count(year, 12)
        )
    }
}
