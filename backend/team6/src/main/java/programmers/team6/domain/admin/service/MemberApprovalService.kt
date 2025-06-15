package programmers.team6.domain.admin.service

import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.dto.response.MemberApprovalResponse
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.member.service.MemberService
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class MemberApprovalService(
    private val memberRepository: MemberRepository,
    private val vacationGrantRuleFinder: VacationGrantRuleFinder,
    private val vacationInfoRepository: VacationInfoRepository,
    private val memberService: MemberService,
    private val vacationInfoLogPublisher: VacationInfoLogPublisher,
) {

    fun findPendingMembers(): List<MemberApprovalResponse> {
        return memberRepository.findPendingMembers(Role.PENDING)
    }

    @Transactional
    fun approveMember(memberId: Long) {
        val member = memberService.findById(memberId)
        member.approve()

        initInfo(member)
    }

    private fun initInfo(member: Member) {
        //TODO : 추후 batch insert를 고민해봐야 할듯
        for (type in VacationCode.entries) {
            val vacationRule = vacationGrantRuleFinder.find(type)
            val vacationInfo = vacationRule.createVacationInfo(member.getId())
//            val vacationInfo = vacationRule.createVacationInfo(member.id)
            vacationInfoRepository.save(vacationInfo)
            vacationInfoLogPublisher.publish(vacationInfo.toLog())
        }
    }

    @Transactional
    fun deleteMember(memberId: Long) {
        val member = memberService.findById(memberId)
        member.validateDeletableOnReject()
        memberRepository.delete(member)
    }
}
