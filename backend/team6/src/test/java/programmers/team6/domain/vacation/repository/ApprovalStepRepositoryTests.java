package programmers.team6.domain.vacation.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.admin.repository.DeptRepository;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse;
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;

@DataJpaTest
@TestInstance(PER_CLASS)
@AutoConfigureTestDatabase(replace = NONE)
class ApprovalStepRepositoryTests {

	@Autowired
	private ApprovalStepRepository approvalStepRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private DeptRepository deptRepository;

	@Autowired
	private CodeRepository codeRepository;

	@Autowired
	private VacationRequestRepository vacationRequestRepository;

	private Long approverId;
	private Long approvalfirstStepId;
	private VacationRequest savedVacation1;
	private ApprovalStep savedApprovalFirstStep;
	private ApprovalStep savedApprovalSecondStep;

	/**
	 *
	 * 휴가 총 4개
	 * 상태 : 승인 2, 반려 1, 대기 1
	 * 날짜 : 8월 3, 9월 1
	 *
	 */
	@BeforeAll
	void setUp() {
		Code savePosition01 = codeRepository.save(new Code("POSITION", "01", "사원"));
		Code savePosition04 = codeRepository.save(new Code("POSITION", "04", "부장"));
		Code saveVacationType01 = codeRepository.save(new Code("VACATION_TYPE", "01", "연차"));
		Dept dept = deptRepository.save(new Dept("인사팀", null));
		Member member = memberRepository.save(
			new Member("민경준", dept, savePosition01, LocalDateTime.of(2025, 1, 1, 0, 0), Role.USER)
		);
		Member approver = memberRepository.save(
			new Member("홍길동", dept, savePosition04, LocalDateTime.of(2025, 1, 1, 0, 0), Role.USER)
		);
		approverId = approver.getId();
		savedVacation1 = vacationRequestRepository.save(
			VacationRequest.builder()
				.member(member)
				.from(LocalDateTime.of(2025, 8, 1, 9, 0))
				.to(LocalDateTime.of(2025, 8, 3, 18, 0))
				.reason("사정이 있습니다.")
				.type(saveVacationType01)
				.status(VacationRequestStatus.APPROVED)
				.build()
		);
		VacationRequest savedVacation2 = vacationRequestRepository.save(
			VacationRequest.builder()
				.member(member)
				.from(LocalDateTime.of(2025, 8, 10, 9, 0))
				.to(LocalDateTime.of(2025, 8, 13, 18, 0))
				.reason("사정이 있습니다.")
				.type(saveVacationType01)
				.status(VacationRequestStatus.APPROVED)
				.build()
		);
		VacationRequest savedVacation3 = vacationRequestRepository.save(
			VacationRequest.builder()
				.member(member)
				.from(LocalDateTime.of(2025, 8, 20, 9, 0))
				.to(LocalDateTime.of(2025, 8, 21, 18, 0))
				.reason("사정이 있습니다.")
				.type(saveVacationType01)
				.status(VacationRequestStatus.REJECTED)
				.build()
		);
		VacationRequest savedVacation4 = vacationRequestRepository.save(
			VacationRequest.builder()
				.member(member)
				.from(LocalDateTime.of(2025, 9, 1, 9, 0))
				.to(LocalDateTime.of(2025, 9, 3, 18, 0))
				.reason("사정이 있습니다.")
				.type(saveVacationType01)
				.status(null)
				.build()
		);

		savedApprovalFirstStep = approvalStepRepository.save(
			new ApprovalStep(null, approver, savedVacation1, ApprovalStatus.APPROVED, 1, null)
		);
		approvalfirstStepId = savedApprovalFirstStep.getId();
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation2, ApprovalStatus.APPROVED, 1, null));
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation3, ApprovalStatus.REJECTED, 1, null));
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation4, ApprovalStatus.PENDING, 1, null));
		savedApprovalSecondStep =
			approvalStepRepository.save(
				new ApprovalStep(null, approver, savedVacation1, ApprovalStatus.APPROVED, 2, null));
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation2, ApprovalStatus.APPROVED, 2, null));
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation3, ApprovalStatus.REJECTED, 2, null));
		approvalStepRepository.save(new ApprovalStep(null, approver, savedVacation4, ApprovalStatus.WAITING, 2, null));

	}

	@Test
	@DisplayName("1차 결재자 ID로 1차 결재 목록을 조회하면 목록이 반환된다")
	void findFirstStep_test() {

		// given
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ApprovalFirstStepSelectResponse> findApprovalFirstStep =
			approvalStepRepository.findFirstStepByMemberId(approverId, 1, pageable);

		// then
		assertThat(findApprovalFirstStep.getContent()).hasSize(4);

	}

	@Test
	@DisplayName("1차 결재자 ID의 1차 결재 목록 중 이름에 민이 들어가고 8월에 승인된 목록은 2개일 것이다")
	void findFirstStepByFilter_test() {

		// given
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ApprovalFirstStepSelectResponse> findApprovalFirstStep = approvalStepRepository.findFirstStepByFilter(
			approverId, null, "민", LocalDateTime.of(2025, 8, 1, 0, 0), LocalDateTime.of(2025, 8, 31, 23, 59),
			ApprovalStatus.APPROVED, 1, pageable
		);

		// then
		assertThat(findApprovalFirstStep.getContent()).hasSize(2);

	}

	@Test
	@DisplayName("2차 결재자 ID로 2차 결재 목록을 조회하면 목록을 반환된다")
	void findSecondStep_test() {

		// given
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ApprovalSecondStepSelectResponse> findApprovalSecondStep =
			approvalStepRepository.findSecondStepByMemberId(approverId, 1, pageable);

		// then
		assertThat(findApprovalSecondStep.getContent()).hasSize(4);

	}

	@Test
	@DisplayName("2차 결재자 ID의 1차 결재 목록 중 이름에 민이 들어가고 8월에 거절된 목록은 1개일 것이다")
	void findSecondStepByFilter_test() {

		// given
		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ApprovalSecondStepSelectResponse> findApprovalSecondStep = approvalStepRepository.findSecondStepByFilter(
			approverId, null, "민", LocalDateTime.of(2025, 8, 1, 0, 0), LocalDateTime.of(2025, 8, 31, 23, 59),
			ApprovalStatus.REJECTED, 1, pageable
		);

		// then
		assertThat(findApprovalSecondStep.getContent()).hasSize(1);

	}

	@Test
	@DisplayName("1차 결재 ID와 결재자 ID를 주면 1차 결재 정보를 반환할 것이다")
	void findByIdAndMemberIdAndStep_test() {

		// when
		ApprovalStep findApproval = approvalStepRepository.findByIdAndMemberIdAndStep(
			approvalfirstStepId, approverId, 1);

		// then
		assertThat(findApproval.getId()).isEqualTo(savedApprovalFirstStep.getId());
		assertThat(findApproval.getStep()).isEqualTo(savedApprovalFirstStep.getStep());
		assertThat(findApproval.getApprovalStatus()).isEqualTo(savedApprovalFirstStep.getApprovalStatus());
		assertThat(findApproval.getMember().getId()).isEqualTo(savedApprovalFirstStep.getMember().getId());
		assertThat(findApproval.getVacationRequest().getId()).isEqualTo(
			savedApprovalFirstStep.getVacationRequest().getId());
	}

	@Test
	@DisplayName("휴가 정보와 step 2를 주면 해당 휴가의 2차 결재 정보가 반환될 것이다")
	void findByVacationRequestAndStep_test() {

		// when
		ApprovalStep findApproval = approvalStepRepository.findByVacationRequestAndStep(savedVacation1, 2);

		// then
		assertThat(findApproval.getId()).isEqualTo(savedApprovalSecondStep.getId());
		assertThat(findApproval.getStep()).isEqualTo(savedApprovalSecondStep.getStep());
		assertThat(findApproval.getApprovalStatus()).isEqualTo(savedApprovalSecondStep.getApprovalStatus());
		assertThat(findApproval.getMember().getId()).isEqualTo(savedApprovalSecondStep.getMember().getId());
		assertThat(findApproval.getVacationRequest().getId()).isEqualTo(
			savedApprovalSecondStep.getVacationRequest().getId());
	}

}