package programmers.team6.domain.admin.support

import lombok.RequiredArgsConstructor
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.repository.VacationInfoLogSearchRepository
import programmers.team6.domain.member.repository.MemberSearchRepository
import java.time.LocalDateTime

@Component
@RequiredArgsConstructor
open class MemberReader(
    private val memberRepository: MemberSearchRepository,
    private val vacationInfoLogSearchRepository: VacationInfoLogSearchRepository
) {

    open fun readHasVacationInfoMemberFrom(request: VacationStatisticsRequest, pageable: Pageable): Members {
        val date = LocalDateTime.of(request.year, 12, 31, 23, 59)

        val ids = vacationInfoLogSearchRepository.queryContainVacationInfoMemberIds(
            date,
            request.vacationCode
        )
        val members = memberRepository.searchFrom(request.deptId, request.name, ids, pageable)
        return Members(members)
    }
}
