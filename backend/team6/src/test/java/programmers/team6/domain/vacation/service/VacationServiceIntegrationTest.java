package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.member.service.DeptService;
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto;
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationInfoSelectResponseDto;
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse;
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto;
import programmers.team6.domain.vacation.dto.request.VacationUpdateResponseDto;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.VacationCode;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;
import programmers.team6.domain.vacation.util.mapper.VacationMapper;

/**
 * VacationService의 코드가 리팩토링이 많이 필요해보임, 리팩토링후 해당 테스트는 쉽게 교체될것같기에 임시로 테스트를 작성하였음
 * @author gunwoong
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({VacationService.class, VacationMapper.class, ApprovalStepService.class, VacationInfoLogPublisher.class,
	DeptService.class, VacationRequestSearchRepository.class})
class VacationServiceIntegrationTest {
	@Autowired
	private VacationService vacationService;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private CodeRepository codeRepository;
	@Autowired
	private DeptRepository deptRepository;
	@Autowired
	private VacationInfoRepository vacationInfoRepository;
	@Autowired
	private VacationRequestRepository vacationRequestRepository;

	@Test
	void should_findMyVacationInfo_when_givenMemberId() {
		// given & when
		Code positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"));
		Dept dept = deptRepository.save(genTestDept("deptName"));
		Member requester = memberRepository.save(genTestMember("A", dept, positionCode));
		vacationInfoRepository.save(new VacationInfo(100d, 5d, VacationCode.ANNUAL.getCode(), requester.getId()));

		// then
		VacationInfoSelectResponseDto response = vacationService.getMyVacationInfo(requester.getId());
		assertThat(response).extracting(VacationInfoSelectResponseDto::getTotalCount,
				VacationInfoSelectResponseDto::getUseCount, VacationInfoSelectResponseDto::getRemainCount)
			.containsExactly(100d, 5d, 100d - 5d);
	}

	@Test
	void should_saveVacation_when_givenValidMemberIdAndVacationCreateRequestDto() {
		// given & when
		Code vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"));
		Code positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"));

		Dept dept = deptRepository.save(genTestDept("deptName"));
		Dept hrDept = deptRepository.save(genTestDept("인사팀"));

		Member requester = memberRepository.save(genTestMember("A", dept, positionCode));
		Member firstApprover = memberRepository.save(genTestMember("B", dept, positionCode));
		Member secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode));
		dept.appointLeader(firstApprover);
		hrDept.appointLeader(secondApprover);

		vacationInfoRepository.save(new VacationInfo(100d, 5d, vacationTypeCode.getCode(), requester.getId()));

		LocalDateTime from = LocalDateTime.now().plusDays(1);
		LocalDateTime to = from.plusDays(1);
		VacationCreateRequestDto vacationCreateRequestDto = VacationCreateRequestDto.builder()
			.from(from)
			.to(to)
			.reason("reason")
			.vacationType(vacationTypeCode.getCode())
			.build();

		// then
		VacationCreateResponseDto response = vacationService.requestVacation(requester.getId(),
			vacationCreateRequestDto);
		assertThat(response)
			.extracting(VacationCreateResponseDto::getFrom, VacationCreateResponseDto::getTo)
			.containsExactly(from, to);
	}

	@Test
	void should_updateVacation_when_givenValidMemberIdAndVacationRequestIdAndVacationUpdateRequestDto() {
		// given & when
		Code vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"));
		Code positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"));

		Dept dept = deptRepository.save(genTestDept("deptName"));
		Dept hrDept = deptRepository.save(genTestDept("인사팀"));

		Member requester = memberRepository.save(genTestMember("A", dept, positionCode));
		Member firstApprover = memberRepository.save(genTestMember("B", dept, positionCode));
		Member secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode));
		dept.appointLeader(firstApprover);
		hrDept.appointLeader(secondApprover);

		vacationInfoRepository.save(new VacationInfo(100d, 5d, vacationTypeCode.getCode(), requester.getId()));

		LocalDateTime from = LocalDateTime.now().plusDays(1);
		LocalDateTime to = from.plusDays(1);

		VacationCreateResponseDto response = vacationService.requestVacation(requester.getId(),
			VacationCreateRequestDto.builder()
				.from(from)
				.to(to)
				.reason("reason")
				.vacationType(vacationTypeCode.getCode())
				.build());

		LocalDateTime updatedFrom = LocalDateTime.now().plusDays(5);
		LocalDateTime updatedTo = updatedFrom.plusDays(1);
		VacationUpdateRequestDto updateRequestDto = new VacationUpdateRequestDto(updatedFrom, updatedTo,
			"updatedReason",
			vacationTypeCode.getCode());

		// then
		VacationUpdateResponseDto result = vacationService.updateVacationRequest(requester.getId(),
			response.getRequestId(), updateRequestDto);

		assertThat(result)
			.extracting(VacationUpdateResponseDto::getRequestId, VacationUpdateResponseDto::getFrom,
				VacationUpdateResponseDto::getTo)
			.containsExactly(response.getRequestId(), updatedFrom, updatedTo);
	}

	@Test
	void should_cancelVacationRequest_when_givenMemberIdAndRequestId() {
		// given & when
		Code vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"));
		Code positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"));

		Dept dept = deptRepository.save(genTestDept("deptName"));
		Dept hrDept = deptRepository.save(genTestDept("인사팀"));

		Member requester = memberRepository.save(genTestMember("A", dept, positionCode));
		Member firstApprover = memberRepository.save(genTestMember("B", dept, positionCode));
		Member secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode));
		dept.appointLeader(firstApprover);
		hrDept.appointLeader(secondApprover);

		vacationInfoRepository.save(new VacationInfo(100d, 5d, vacationTypeCode.getCode(), requester.getId()));

		LocalDateTime from = LocalDateTime.now().plusDays(1);
		LocalDateTime to = from.plusDays(1);
		VacationCreateRequestDto vacationCreateRequestDto = VacationCreateRequestDto.builder()
			.from(from)
			.to(to)
			.reason("reason")
			.vacationType(vacationTypeCode.getCode())
			.build();
		VacationCreateResponseDto response = vacationService.requestVacation(requester.getId(),
			vacationCreateRequestDto);

		// then
		vacationService.cancelVacationRequest(requester.getId(), response.getRequestId());
		Optional<VacationRequest> result = vacationRequestRepository.findById(response.getRequestId());
		assertThat(result).isPresent();
		assertThat(result.get().getStatus()).isEqualTo(VacationRequestStatus.CANCELED);
	}

	@Test
	void should_selectVacationCalendar_when_givenYearAndDeptId() {
		// given & when
		Code vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"));
		Code positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"));

		Dept dept = deptRepository.save(genTestDept("deptName"));
		Dept hrDept = deptRepository.save(genTestDept("인사팀"));

		Member requester = memberRepository.save(genTestMember("A", dept, positionCode));
		Member firstApprover = memberRepository.save(genTestMember("B", dept, positionCode));
		Member secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode));
		dept.appointLeader(firstApprover);
		hrDept.appointLeader(secondApprover);

		vacationInfoRepository.save(new VacationInfo(100d, 5d, vacationTypeCode.getCode(), requester.getId()));

		LocalDateTime from = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
		LocalDateTime to = from.plusDays(1);
		VacationCreateRequestDto vacationCreateRequestDto = VacationCreateRequestDto.builder()
			.from(from)
			.to(to)
			.reason("reason")
			.vacationType(vacationTypeCode.getCode())
			.build();
		VacationCreateResponseDto response = vacationService.requestVacation(requester.getId(),
			vacationCreateRequestDto);
		vacationRequestRepository.findById(response.getRequestId()).ifPresent(result -> result.approve());

		// then
		List<VacationRequestCalendarResponse> result = vacationService.selectVacationCalendar(
			"2025-01", dept.getId());
		assertThat(result).hasSize(1);
	}
}