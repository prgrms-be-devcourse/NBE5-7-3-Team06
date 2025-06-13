package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationListResponseDto;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository;
import programmers.team6.domain.vacation.util.mapper.VacationMapper;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.NotFoundException;
import programmers.team6.support.MemberMother;

@ExtendWith(SpringExtension.class)
@Import(VacationMapper.class)
class VacationServiceTests {
	@Mock
	private VacationMapper vacationMapper;
	@Mock
	private VacationInfoRepository vacationInfoRepository;
	@Mock
	private VacationRequestRepository vacationRequestRepository;
	@Mock
	private ApprovalStepRepository approvalStepRepository;
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private CodeRepository codeRepository;
	@Mock
	private ApprovalStepService approvalStepService;
	@Mock
	private VacationRequestSearchRepository vacationRequestSearchRepository;
	@InjectMocks
	private VacationService vacationService;

	@Nested
	class getMyVacationInfo {
		@Test
		void should_throwRuntimeException_when_givenNotExistMemberId() {
			// when
			when(memberRepository.findById(0L)).thenReturn(Optional.empty());

			// then
			assertThatThrownBy(() -> vacationService.getMyVacationInfo(0L)).isInstanceOf(RuntimeException.class);
		}
	}

	@Nested
	class requestVacation {
		@Test
		void should_throwRuntimeException_when_givenNotExistVacationInfo() {
			// when
			when(memberRepository.findByIdWithDeptAndLeader(0L)).thenReturn(Optional.empty());

			// then
			assertThatThrownBy(() -> vacationService.requestVacation(0L, null)).isInstanceOf(RuntimeException.class);
		}

		@Test
		void should_throwBadRequestException_when_givenInvalidFromAndTo() {
			// given
			Member member = MemberMother.member();
			LocalDateTime from = LocalDateTime.now().plusDays(1);
			LocalDateTime to = LocalDateTime.now();
			VacationCreateRequestDto vacationCreateRequestDto = new VacationCreateRequestDto(from, to, "empty",
				"empty");

			// when
			when(memberRepository.findByIdWithDeptAndLeader(0L)).thenReturn(Optional.of(member));
			when(vacationRequestRepository.countInRangeFromBetweenToBy(member.getId(),
				from, to)).thenReturn(1l);

			// then
			assertThatThrownBy(
				() -> vacationService.requestVacation(member.getId(), vacationCreateRequestDto)).isInstanceOf(
				BadRequestException.class);
		}

		@Test
		void should_throwNotFoundException_when_givenInvalidMemberIdAndVacationInfoType() {
			// given
			Member member = MemberMother.member();
			LocalDateTime from = LocalDateTime.now().plusDays(1);
			LocalDateTime to = LocalDateTime.now();
			String vacationType = "vacationType";
			VacationCreateRequestDto vacationCreateRequestDto = new VacationCreateRequestDto(from, to, "reason",
				vacationType);

			// when
			when(memberRepository.findByIdWithDeptAndLeader(0L)).thenReturn(Optional.of(member));
			when(vacationRequestRepository.countInRangeFromBetweenToBy(member.getId(),
				vacationCreateRequestDto.getFrom(), vacationCreateRequestDto.getTo())).thenReturn(0l);
			when(vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)).thenReturn(0d);
			when(vacationInfoRepository.findActualRemainingVacationDays(member.getId(), vacationType)).thenReturn(
				Optional.empty());

			// then
			assertThatThrownBy(
				() -> vacationService.requestVacation(member.getId(), vacationCreateRequestDto)).isInstanceOf(
				NotFoundException.class);
		}

		@Test
		void should_throwBadRequestException_when_actualRemainCountLessThanRequestDays() {
			// given
			Member member = MemberMother.member();
			LocalDateTime from = LocalDateTime.now().plusDays(1);
			LocalDateTime to = LocalDateTime.now();
			String vacationType = "vacationType";
			VacationCreateRequestDto vacationCreateRequestDto = new VacationCreateRequestDto(from, to, "reason",
				vacationType);

			// when
			when(memberRepository.findByIdWithDeptAndLeader(0L)).thenReturn(Optional.of(member));
			when(vacationRequestRepository.countInRangeFromBetweenToBy(member.getId(),
				vacationCreateRequestDto.getFrom(), vacationCreateRequestDto.getTo())).thenReturn(0l);
			when(vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)).thenReturn(5d);
			when(vacationInfoRepository.findActualRemainingVacationDays(member.getId(), vacationType)).thenReturn(
				Optional.of(0d));

			// then
			assertThatThrownBy(
				() -> vacationService.requestVacation(member.getId(), vacationCreateRequestDto)).isInstanceOf(
				BadRequestException.class);
		}

		@Test
		void should_throwRuntimeException_when_givenInvalidGroupCodeAndCode() {
			// given
			Member member = MemberMother.member();
			Member leader = MemberMother.member();
			member.getDept().appointLeader(leader);

			LocalDateTime from = LocalDateTime.now().plusDays(1);
			LocalDateTime to = LocalDateTime.now();
			String vacationType = "vacationType";
			VacationCreateRequestDto vacationCreateRequestDto = new VacationCreateRequestDto(from, to, "reason",
				vacationType);

			// when
			when(memberRepository.findByIdWithDeptAndLeader(0L)).thenReturn(Optional.of(member));
			when(vacationRequestRepository.countInRangeFromBetweenToBy(member.getId(),
				vacationCreateRequestDto.getFrom(), vacationCreateRequestDto.getTo())).thenReturn(0l);
			when(vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)).thenReturn(0d);
			when(vacationInfoRepository.findActualRemainingVacationDays(member.getId(), vacationType)).thenReturn(
				Optional.of(1d));

			// then
			assertThatThrownBy(
				() -> vacationService.requestVacation(member.getId(), vacationCreateRequestDto)).isInstanceOf(
				RuntimeException.class);
		}

	}

	@Test
	void should_saveVacation_when_givenValidMemberIdAndVacationCreateRequestDto() {
		// given
		Member member = MemberMother.member();
		Member leader = MemberMother.member();
		member.getDept().appointLeader(leader);

		LocalDateTime from = LocalDateTime.now();
		LocalDateTime to = from.plusDays(2);
		VacationCreateRequestDto vacationCreateRequestDto = new VacationCreateRequestDto(from, to, "reason",
			"vacationType");

		// when
		when(memberRepository.findByIdWithDeptAndLeader(member.getId())).thenReturn(Optional.of(member));
		when(vacationRequestRepository.countInRangeFromBetweenToBy(member.getId(), from, to)).thenReturn(0l);
		when(vacationRequestRepository.calculateRequestedVacationDays(from, to,
			vacationCreateRequestDto.getVacationType())).thenReturn((double)Duration.between(from, to).toDays());
		when(vacationInfoRepository.findActualRemainingVacationDays(member.getId(),
			vacationCreateRequestDto.getVacationType())).thenReturn(Optional.of(30d));
		when(codeRepository.findByGroupCodeAndCode("VACATION_TYPE",
			vacationCreateRequestDto.getVacationType())).thenReturn(Optional.empty());

		// then
		assertThatThrownBy(
			() -> vacationService.requestVacation(member.getId(), vacationCreateRequestDto)).isInstanceOf(
			RuntimeException.class);
	}

	@Nested
	class GetVacationRequestList {

		@Test
		void should_returnEmptyResult_when_findIdPageIsEmpty() {
			// given
			Member member = MemberMother.member();

			// when
			when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
			when(vacationRequestRepository.findIdsByRequesterIdPaging(anyLong(), any(Pageable.class))).thenReturn(
				Page.empty());

			// then
			VacationListResponseDto result = vacationService.getVacationRequestList(member.getId(), 0);
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getPageNumber()).isEqualTo(0);
		}
	}

	@Nested
	class UpdateVacationRequest {
		/**
		 * requestVacation 메서드와 겹치는게 많아서 리팩토링 후 테스트 추가가 필요해 보임
		 * @author gunwoong
		 */
	}

}