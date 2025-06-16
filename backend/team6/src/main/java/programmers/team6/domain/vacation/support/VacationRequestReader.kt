package programmers.team6.domain.vacation.support

import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException
import java.util.function.Supplier

@Component
open class VacationRequestReader(
    private val vacationRequestRepository: VacationRequestRepository,
    private val approvalStepRepository: ApprovalStepRepository
) {

    open fun readDetailFrom(id: Long): VacationRequestDetailReadResponse {
        return vacationRequestRepository.findVacationRequestDetailById2(id)
            .orElseThrow(Supplier { NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST) })
            .injectApprovalStepDetails(readApprovalFrom(id))
    }

    private fun readApprovalFrom(id: Long): List<ApprovalStepDetailUpdateResponse> {
        val approvalStepDetailUpdateResponses: List<ApprovalStepDetailUpdateResponse> =
            approvalStepRepository.findApprovalStepDetailById(id)
        if (approvalStepDetailUpdateResponses.isEmpty()) {
            throw NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP)
        }
        return approvalStepDetailUpdateResponses
    }
}
