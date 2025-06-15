package programmers.team6.mock

import io.mockk.mockk
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.repository.VacationInfoLogSearchRepository
import programmers.team6.domain.admin.support.MemberReader
import programmers.team6.domain.admin.support.Members
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.member.repository.MemberSearchRepository
import java.util.*

class MemberReaderFake(vararg members: Member?) : MemberReader(mockk<MemberSearchRepository>(), mockk<VacationInfoLogSearchRepository>()) {
    private val members: List<Member> =
        ArrayList(
            Arrays.asList(*members)
        )

    override fun readHasVacationInfoMemberFrom(request: VacationStatisticsRequest, pageable: Pageable): Members {
        return Members(PageImpl(members, pageable, members.size.toLong()))
    }
}
