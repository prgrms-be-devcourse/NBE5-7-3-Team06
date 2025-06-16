package programmers.team6.domain.admin.service

import io.mockk.*
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.member.service.MemberService
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.support.MemberMother

internal class MemberApprovalServiceTests {

    private val memberRepository = mockk<MemberRepository>()

    private val vacationGrantRuleFinder = spyk<VacationGrantRuleFinder>()

    private val vacationInfoRepository = mockk<VacationInfoRepository>()

    private val memberService = mockk<MemberService>()

    private val vacationInfoLogPublisher = mockk<VacationInfoLogPublisher>()

    private val memberApprovalService = MemberApprovalService(
        memberRepository,
        vacationGrantRuleFinder,
        vacationInfoRepository,
        memberService,
        vacationInfoLogPublisher
    )

    @Test
    @DisplayName("회원 승인 성공 테스트")
    fun approveMember_success() {
        // given
        val id = 1L
        val member = MemberMother.withIdAndRole(id, Role.PENDING)

        every { memberService.findById(id) } returns member
        every { vacationInfoRepository.save(any()) } returns mockk()
        every { vacationInfoLogPublisher.publish(any<VacationInfoLog>()) } just Runs

        // when
        memberApprovalService.approveMember(id)

        // then
        AssertionsForClassTypes.assertThat(member.role).isEqualTo(Role.USER)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("회원 승인 시 Role PENDING이 아니면 BadRequestException 예외가 발생")
    fun approveMember_notPending_throwsBadRequestException(role: Role?) {
        // given
        val id = 1L
        val member = MemberMother.withIdAndRole(id, role)

        every { memberService.findById(id) } returns member

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            memberApprovalService.approveMember(id)
        }.isInstanceOf(BadRequestException::class.java)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("회원 반려 시 Role PENDING이 아니면 BadRequestException 예외가 발생")
    fun deleteMember_notPending_throwsBadRequestException(role: Role?) {
        // given
        val id = 1L
        val member = MemberMother.withIdAndRole(id, role)

        every { memberService.findById(id) } returns member

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            memberApprovalService.deleteMember(id)
        }.isInstanceOf(BadRequestException::class.java)
    }
}