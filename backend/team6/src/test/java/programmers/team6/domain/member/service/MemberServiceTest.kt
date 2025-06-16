package programmers.team6.domain.member.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.findByIdOrNull
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.support.MemberMother
import java.util.*


internal class MemberServiceTest {

    private val memberRepository: MemberRepository = mockk<MemberRepository>()

    @Test
    @DisplayName("사용자 찾기 성공")
    fun find_member_success() {
        val id = 1L
        val member = MemberMother.withId(id)

        every { memberRepository.findByIdOrNull(id) } returns member
        val memberService = MemberService(memberRepository)

        val result = memberService.findById(id)

        Assertions.assertThat(member).isEqualTo(result)
    }

    @Test
    @DisplayName("사용자 찾기 실패")
    fun find_member_failure() {
        val id = 1L

        every { memberRepository.findByIdOrNull(id) } returns null

        val memberService = MemberService(memberRepository)

        Assertions.assertThatThrownBy { memberService.findById(id) }.isInstanceOf(
            NotFoundException::class.java
        )
            .hasFieldOrPropertyWithValue(
                "errorCode",
                NotFoundErrorCode.NOT_FOUND_MEMBER
            )
    }
}