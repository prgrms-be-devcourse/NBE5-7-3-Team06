package programmers.team6.domain.vacation.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRule
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.domain.vacation.support.VacationInfos
import java.time.LocalDate

@Service
class VacationGrantService(
    private val memberRepository: MemberRepository,
    private val vacationInfoRepository: VacationInfoRepository,
    private val vacationGrantRuleFinder: VacationGrantRuleFinder,
    private val vacationInfoLogPublisher: VacationInfoLogPublisher
) {

    @Transactional
    fun grantAnnualVacations(date: LocalDate) {
        val rules = vacationGrantRuleFinder.findAll()
        val vacationInfos = rules.getRules()
            .flatMap { rule ->
                val baseLineDates = rule.getBaseLineDates(date)
                selectVacationInfo(rule, baseLineDates)
            }
            .let(::VacationInfos)

        vacationInfos.memberIds.forEach { id ->
            val memberInfos = vacationInfos.getByMemberId(id)
            val member = memberRepository.findByIdOrNull(id) ?: throw RuntimeException("Member not found: $id")
            val logs = rules.grant(date, member, VacationInfos(memberInfos))
            vacationInfoLogPublisher.publish(logs)
        }
    }

    private fun selectVacationInfo(rule: VacationGrantRule, baseLineDates: List<LocalDate>): List<VacationInfo> =
        if (rule.isSameType(VacationCode.ANNUAL)) {
            vacationInfoRepository.findAnnualVacationByJoinDates(VacationCode.ANNUAL.code, baseLineDates)
        } else {
            vacationInfoRepository.findByTypeAndCreatedAtToDate(rule.typeCode, baseLineDates)
        }
}
