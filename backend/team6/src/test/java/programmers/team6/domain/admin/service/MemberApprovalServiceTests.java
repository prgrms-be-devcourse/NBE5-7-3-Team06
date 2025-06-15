package programmers.team6.domain.admin.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.params.provider.EnumSource.Mode.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.member.service.MemberService;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.support.MemberMother;

@ExtendWith(MockitoExtension.class)
class MemberApprovalServiceTests {

	@Mock
	private MemberRepository memberRepository;

	@Spy
	private VacationGrantRuleFinder vacationGrantRuleFinder;

	@Mock
	private VacationInfoRepository vacationInfoRepository;

	@Mock
	private MemberService memberService;

	@Mock
	private VacationInfoLogPublisher vacationInfoLogPublisher;

	@InjectMocks
	private MemberApprovalService memberApprovalService;

	@Test
	@DisplayName("회원 승인 성공 테스트")
	void approveMember_success() {

		// given
		Long id = 1L;
		Member member = MemberMother.withIdAndRole(id, Role.PENDING);

		when(memberService.findById(id)).thenReturn(member);
		when(vacationInfoRepository.save(any())).thenReturn(null);
		doNothing().when(vacationInfoLogPublisher).publish(any(VacationInfoLog.class));

		// when
		memberApprovalService.approveMember(id);

		// then
		assertThat(member.getRole()).isEqualTo(Role.USER);

	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("회원 승인 시 Role PENDING이 아니면 BadRequestException 예외가 발생")
	void approveMember_notPending_throwsBadRequestException(Role role) {

		// given
		Long id = 1L;
		Member member = MemberMother.withIdAndRole(id, role);

		when(memberService.findById(id)).thenReturn(member);

		// then
		assertThatThrownBy(
			() -> {
				memberApprovalService.approveMember(id);
			}
		).isInstanceOf(BadRequestException.class);

	}

	@ParameterizedTest
	@EnumSource(mode = EXCLUDE, names = "PENDING")
	@DisplayName("회원 반려 시 Role PENDING이 아니면 BadRequestException 예외가 발생")
	void deleteMember_notPending_throwsBadRequestException(Role role) {

		// given
		Long id = 1L;
		Member member = MemberMother.withIdAndRole(id, role);

		when(memberService.findById(id)).thenReturn(member);

		// then
		assertThatThrownBy(
			() -> {
				memberApprovalService.deleteMember(id);
			}
		).isInstanceOf(BadRequestException.class);

	}

}