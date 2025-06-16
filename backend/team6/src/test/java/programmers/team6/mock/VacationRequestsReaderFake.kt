package programmers.team6.mock

import io.mockk.mockk
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.support.VacationRequests
import programmers.team6.domain.admin.support.VacationRequestsReader
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import java.util.*

class VacationRequestsReaderFake(vararg vacationRequests: VacationRequest) : VacationRequestsReader(mockk<VacationRequestRepository>()) {
    private val vacationRequests: List<VacationRequest> =
        ArrayList(Arrays.asList(*vacationRequests))

    override fun vacationRequestFrom(ids: List<Long>, request: VacationStatisticsRequest): VacationRequests {
        return VacationRequests(vacationRequests)
    }
}
