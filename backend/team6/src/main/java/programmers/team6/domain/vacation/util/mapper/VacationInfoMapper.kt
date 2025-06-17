package programmers.team6.domain.vacation.util.mapper

import org.springframework.data.domain.Page
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.dto.response.MemberVacationInfoSelectResponse
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponse
import programmers.team6.domain.vacation.entity.VacationInfo

object VacationInfoMapper {
    fun toMemberVacationInfoSelectResponsePageFrom(
        members: Page<Member>,
        vacationInfos: List<VacationInfo>
    ): Page<MemberVacationInfoSelectResponse> {
        return members.map { member: Member ->
            this.toMemberVacationSelectResponse(
                member,
                vacationInfos.filter { it.memberId == member.id }
            )
        }
    }

    private fun toMemberVacationSelectResponse(
        member: Member,
        vacationInfos: List<VacationInfo>
    ): MemberVacationInfoSelectResponse {
        val responses  = vacationInfos.map { toVacationInfoSelectResponseFrom(it) }
        return MemberVacationInfoSelectResponse(member.id!!, member.name, responses)
    }

    private fun toVacationInfoSelectResponseFrom(info: VacationInfo): VacationInfoSelectResponse =
        VacationInfoSelectResponse(
            info.vacationId!!,
            info.totalCount,
            info.vacationType,
            info.version
        )

}
