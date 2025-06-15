package programmers.team6.domain.admin.service

import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.support.MemberReader
import programmers.team6.domain.admin.support.VacationInfoLogReader
import programmers.team6.domain.admin.support.VacationRequestsReader
import programmers.team6.domain.admin.utils.mapper.VacationStatisticsMapper
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.response.VacationMonthlyStatisticsResponse

@Service
@RequiredArgsConstructor
class VacationStatisticsService(
    private val memberRepository: MemberRepository,
    private val vacationInfoLogReader: VacationInfoLogReader,
    private val vacationRequestsReader: VacationRequestsReader,
    private val memberReader: MemberReader,
    private val mapper: VacationStatisticsMapper
) {

    @Transactional(readOnly = true)
    fun getMonthlyVacationStatistics(
        request: VacationStatisticsRequest,
        pageable: Pageable?
    ): Page<VacationMonthlyStatisticsResponse> {
        val members = memberReader.readHasVacationInfoMemberFrom(request, pageable)
        val vacationRequests = vacationRequestsReader.vacationRequestFrom(members.toIds(), request)
        val logs = vacationInfoLogReader.lastedLogsFrom(members.toIds(), request)
        return mapper.toDto(members, vacationRequests, logs, request.year)
    }
}
