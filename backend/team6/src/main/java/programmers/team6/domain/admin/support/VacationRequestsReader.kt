package programmers.team6.domain.admin.support

import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.vacation.repository.VacationRequestRepository

@Component
@RequiredArgsConstructor
open class VacationRequestsReader(private val vacationRequestRepository: VacationRequestRepository) {

    open fun vacationRequestFrom(ids: List<Long>, request: VacationStatisticsRequest): VacationRequests {
        return VacationRequests(
            vacationRequestRepository.findByMemberIdInAndYear(ids, request.year, codes(request.vacationCode))
        )
    }

    private fun codes(code: String): List<String> {
        if (code == "01") {
            return listOf("01", "05")
        }
        return listOf(code)
    }
}
