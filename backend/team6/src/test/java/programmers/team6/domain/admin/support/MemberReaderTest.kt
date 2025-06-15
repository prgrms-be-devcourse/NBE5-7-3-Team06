package programmers.team6.domain.admin.support

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.admin.repository.VacationInfoLogSearchRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberSearchRepository
import programmers.team6.support.MemberMother
import java.time.LocalDateTime
import java.util.List

internal class MemberReaderTest {
    @Test
    fun 휴가정보가_있는_사원목록_조회() {
        val year = 2024
        val vacationCode = "01"
        val deptId = 10L
        val name = "홍길동"
        val pageable: Pageable = PageRequest.of(0, 10)
        val expectedDate = LocalDateTime.of(year, 12, 31, 23, 59)
        val memberIds = listOf(1L, 2L)

        val vacationInfoLogSearchRepository = mockk<VacationInfoLogSearchRepository>()
        every { vacationInfoLogSearchRepository.queryContainVacationInfoMemberIds(expectedDate, vacationCode) }.returns(
            memberIds
        )

        val member1 = MemberMother.withId(1L) // 테스트용 더미 객체. 필요시 ID 설정
        val member2 = MemberMother.withId(2L)
        val mockPage: Page<Member> = PageImpl(List.of(member1, member2))

        val memberRepository = mockk<MemberSearchRepository>()
        every { memberRepository.searchFrom(deptId, name, memberIds, pageable) }.returns(mockPage)

        val memberReader = MemberReader(memberRepository, vacationInfoLogSearchRepository)

        val request = VacationStatisticsRequest(year, name, deptId, vacationCode)

        val result = memberReader.readHasVacationInfoMemberFrom(request, pageable)

        assertThat(result).isEqualTo(Members(mockPage))
    }
}