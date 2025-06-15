package programmers.team6.domain.admin.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.dto.request.VacationRequestDetailUpdateRequest
import programmers.team6.domain.admin.dto.response.AdminVacationRequestSearchResponse
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchCustom
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.support.VacationRequestReader
import programmers.team6.global.exception.code.ConflictErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.ConflictException
import programmers.team6.global.exception.customException.NotFoundException

@Service
class AdminService(
    private val adminVacationRequestSearchCustom: AdminVacationRequestSearchCustom,
    private val vacationRequestRepository: VacationRequestRepository,
    private val codeRepository: CodeRepository,
    private val approvalStepRepository: ApprovalStepRepository,
    private val vacationRequestReader: VacationRequestReader
) {

    @Transactional(readOnly = true)
    fun search(
        pageable: Pageable,
        searchCondition: AdminVacationSearchCondition
    ): AdminVacationRequestSearchResponse {
        return AdminVacationRequestSearchResponse(
            adminVacationRequestSearchCustom.search(searchCondition, pageable),
            codeRepository.findCodeInfosByGroupCode("POSITION"),
            codeRepository.findCodeInfosByGroupCode("VACATION_TYPE")
        )
    }

    @Transactional(readOnly = true)
    fun selectVacationRequestDetailById(id: Long): VacationRequestDetailReadResponse? {
        return vacationRequestReader.readDetailFrom(id)
    }

    @Transactional
    fun updateVacationRequestDetailById(
        id: Long,
        vacationRequestDetailUpdateRequest: VacationRequestDetailUpdateRequest
    ) {
        val vacationRequest = vacationRequestRepository.findVacationRequestById(id)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST)

        val vacationRequestType = codeRepository.findByIdAndGroupCode(
            vacationRequestDetailUpdateRequest.typeId,
            "VACATION_TYPE"
        ) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_CODE)

        vacationRequest.update(
            vacationRequestType, vacationRequestDetailUpdateRequest.from,
            vacationRequestDetailUpdateRequest.to, vacationRequestDetailUpdateRequest.vacationRequestStatus,
            vacationRequestDetailUpdateRequest.reason
        )

        val approvalSteps = approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(
            id
        )
        if (approvalSteps.isEmpty()
            || vacationRequestDetailUpdateRequest.approvalReason.size != approvalSteps.size
        ) {
            throw ConflictException(ConflictErrorCode.CONFLICT_APPROVAL_STEP)
        }

        for (i in approvalSteps.indices) {
            approvalSteps.get(i).update(vacationRequestDetailUpdateRequest.approvalReason.get(i))
        }
    }
}
