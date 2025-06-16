package programmers.team6.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.global.exception.code.NotFoundErrorCode;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.support.MemberMother;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Test
	@DisplayName("사용자 찾기 성공")
	void find_member_success() {
		long id = 1L;
		Member member = MemberMother.withId(id);
		when(memberRepository.findById(id)).thenReturn(Optional.of(member));
		MemberService memberService = new MemberService(memberRepository);

		Member result = memberService.findById(id);

		assertThat(member).isEqualTo(result);
	}

	@Test
	@DisplayName("사용자 찾기 실패")
	void find_member_failure() {
		long id = 1L;
		when(memberRepository.findById(id)).thenReturn(Optional.empty());
		MemberService memberService = new MemberService(memberRepository);

		assertThatThrownBy(() -> memberService.findById(id)).isInstanceOf(NotFoundException.class)
			.hasFieldOrPropertyWithValue("errorCode",
				NotFoundErrorCode.NOT_FOUND_MEMBER);

	}
}