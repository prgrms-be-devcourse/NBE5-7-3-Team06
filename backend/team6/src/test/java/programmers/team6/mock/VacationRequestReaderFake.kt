package programmers.team6.mock

import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.support.VacationRequestReader
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

class VacationRequestReaderFake(
    vacationRequestRepository: VacationRequestRepository,
    approvalStepRepository: ApprovalStepRepository
) : VacationRequestReader(vacationRequestRepository, approvalStepRepository) {
    private val vacationRequestRepository: MutableMap<Long, VacationRequestDetailReadResponse> =
        mutableMapOf()
    private val approvalStepRepository: MutableMap<Long, MutableList<ApprovalStepDetailUpdateResponse>> =
        mutableMapOf()

    override fun readDetailFrom(id: Long): VacationRequestDetailReadResponse {
        if (!vacationRequestRepository.containsKey(id)) {
            throw NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST)
        }

        if (!approvalStepRepository.containsKey(id) || approvalStepRepository.get(id)!!.isEmpty()) {
            throw NotFoundException(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP)
        }

        return vacationRequestRepository.get(id)!!.injectApprovalStepDetails(approvalStepRepository.get(id)!!.toList())
    }

    fun putVacationRequestDetail(key: Long, value: VacationRequestDetailReadResponse) {
        vacationRequestRepository.put(key, value)
    }

    fun putApprovalStep(key: Long, value: MutableList<ApprovalStepDetailUpdateResponse>) {
        approvalStepRepository.put(key, value)
    }
}
