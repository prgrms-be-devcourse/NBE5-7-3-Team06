package programmers.team6.domain.vacation.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequest
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequests
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequestsList
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRule
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.domain.vacation.support.VacationInfos
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.code.ConflictErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.ConflictException
import java.util.*

@Service
class VacationInfoService(
    private val vacationInfoRepository: VacationInfoRepository,
    private val vacationGrantRuleFinder: VacationGrantRuleFinder,
    private val vacationInfoLogPublisher: VacationInfoLogPublisher
) {

    @Transactional
    fun updateFrom(request: VacationInfoUpdateTotalCountRequestsList) {
        val vacationInfos = findVacationInfos(request.vacationIds())
        updateVacationInfos(request, vacationInfos)
    }

    private fun findVacationInfos(ids: List<Int>): VacationInfos {
        val vacationInfos: List<VacationInfo> = vacationInfoRepository.findAllByVacationIdIn(ids)
        return VacationInfos(vacationInfos)
    }

    private fun updateVacationInfos(
        request: VacationInfoUpdateTotalCountRequestsList,
        vacationInfos: VacationInfos
    ) {
        for (vacations in request.requests) {
            updateTotalCount(vacationInfos.getByMemberId(vacations.memberId), vacations)
        }
    }

    private fun updateTotalCount(infos: List<VacationInfo>, requests: VacationInfoUpdateTotalCountRequests) {
        for (info in infos) {
            val target: VacationInfoUpdateTotalCountRequest? = requests.getTarget(info.vacationType)
            if (target == null) {
                continue
            }
            val request = target
            val vacationGrantRule: VacationGrantRule = vacationGrantRuleFinder.find(info.vacationType)

            validUpdate(info, vacationGrantRule, request)

            val log = info.updateTotalCount(request.totalCount)
            vacationInfoLogPublisher!!.publish(log)
        }
    }

    private fun validUpdate(
        info: VacationInfo, vacationGrantRule: VacationGrantRule,
        request: VacationInfoUpdateTotalCountRequest
    ) {
        if (!vacationGrantRule.canUpdate(request.totalCount)) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_INVALID_INPUT)
        }
        if (!info.isSameVersion(request.version)) {
            throw ConflictException(ConflictErrorCode.CONFLICT_VERSION)
        }
    }
}
