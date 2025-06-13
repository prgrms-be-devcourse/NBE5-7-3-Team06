package programmers.team6.domain.admin.repository;

import static org.assertj.core.api.Assertions.*;
import static programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.*;
import static programmers.team6.domain.admin.repository.TestVacationRequestSearchConditionFactory.*;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import lombok.extern.slf4j.Slf4j;
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition;
import programmers.team6.domain.admin.dto.response.VacationRequestSearchResponse;
import programmers.team6.domain.admin.enums.Quarter;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;
import programmers.team6.global.exception.customException.BadRequestException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AdminVacationRequestSearchCustom.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class AdminVacationRequestSearchCustomTest {

	@Autowired
	private AdminVacationRequestSearchCustom adminVacationRequestSearchCustom;
	@Autowired
	private VacationRequestRepository vacationRequestRepository;
	@Autowired
	private ApprovalStepRepository approvalStepRepository;
	@Autowired
	private CodeRepository codeRepository;
	@Autowired
	private DeptRepository deptRepository;
	@Autowired
	private MemberRepository memberRepository;

	private final Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

	/**
	 * 시나리오
	 * 각각의 휴가 기간 (현재 년도)~(현재 년도 + 2)년, 1월~12월, 각각 1일부터 마지막날까지
	 * 3명의 각기다른 직급과 부서, 휴가타입의 휴가 신청자, 부서별 직급이 같은 1차 결재자, 공통 2차 결재자
	 *
	 * ex)
	 * 날짜
	 * 2025년 1월 1일~31일, 2025년 2월 1일~28일..., 2026 12월 1일~31일
	 * 결재 라인 및 직급 이름
	 * A(부서0, 직급0, 휴가0) - D1(부서0, 직급3) - E(부서0, 직급4)
	 * B(부서1, 직급1, 휴가1) - D2(부서1, 직급3) - E(부서0, 직급4)
	 * C(부서2, 직급2, 휴가2) - D3(부서2, 직급3) - E(부서0, 직급4)
	 *
	 * 전체 휴가 신청 개수 = 2 x 12
	 */
	private static final int VACATION_REQUESTER_CNT = 3;
	private static final int START_YEAR = Year.now().getValue() + 1;
	private static final int END_YEAR = START_YEAR + 1;
	private static final int YEAR_DURATION = END_YEAR - START_YEAR + 1;
	private static final int END_OF_MONTH = 12;
	private static final int VACATION_REQUEST_CNT_PER_MONTH = 1;

	private static final int VACATION_TYPE_CNT = 3;
	private static final int DEPT_CNT = 3;
	private static final int POSITION_CNT = 5;
	private static final int FIRST_APPROVER_POSITION_IDX = 3;
	private static final int SECOUND_APPROVER_POSITION_IDX = 4;
	private static final int TOTAL_VACATION_REQUESTS_CNT =
		VACATION_REQUESTER_CNT * YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH;

	private static final String PREFIX_VACATION_TYPE = "휴가";
	private static final String PREFIX_DEPT = "부서";
	private static final String PREFIX_POSITION = "직급";

	private List<Code> vacationTypeCodes;
	private List<Code> positionCodes;
	private List<Dept> depts;
	private List<Member> vacationRequesters;
	private List<Member> firstApprovers;
	private Member secondApprover;

	@BeforeAll
	void setUpOnce() {
		setupCodes();
		setupDepartments();
		setupMembers();
		setupVacationRequests();
	}

	private void setupCodes() {
		// 휴가 타입 코드
		vacationTypeCodes = genTestCodeList(UUID.randomUUID().toString(), VACATION_TYPE_CNT, PREFIX_VACATION_TYPE);
		codeRepository.saveAll(vacationTypeCodes);

		// 부서 코드
		codeRepository.saveAll(genTestCodeList(UUID.randomUUID().toString(), DEPT_CNT, PREFIX_DEPT));

		// 직급 코드
		positionCodes = genTestCodeList(UUID.randomUUID().toString(), POSITION_CNT, PREFIX_POSITION);
		codeRepository.saveAll(positionCodes);
	}

	private void setupDepartments() {
		depts = genTestDeptList(DEPT_CNT, PREFIX_DEPT);
		deptRepository.saveAll(depts);
	}

	private void setupMembers() {
		// 2차 결재자
		secondApprover = genTestMember("D", depts.get(0), positionCodes.get(SECOUND_APPROVER_POSITION_IDX));
		memberRepository.save(secondApprover);

		// 1차 결재자들
		firstApprovers = genTestMemberList(DEPT_CNT, "D", depts.subList(0, DEPT_CNT),
			positionCodes.get(FIRST_APPROVER_POSITION_IDX));
		memberRepository.saveAll(firstApprovers);

		// 휴가 신청자들
		vacationRequesters = genTestMemberList(VACATION_REQUESTER_CNT, 'A', depts.subList(0, DEPT_CNT),
			positionCodes.subList(0, VACATION_REQUESTER_CNT));
		memberRepository.saveAll(vacationRequesters);

		// 부서 리더 지정
		for (int i = 0; i < DEPT_CNT; i++) {
			depts.get(i).appointLeader(firstApprovers.get(i));
		}
	}

	private void setupVacationRequests() {
		for (int vacationRequesterIdx = 0; vacationRequesterIdx < VACATION_REQUESTER_CNT; vacationRequesterIdx++) {
			for (int year = START_YEAR; year <= END_YEAR; year++) {
				for (int month = 1; month <= END_OF_MONTH; month++) {
					for (int i = 0; i < VACATION_REQUEST_CNT_PER_MONTH; i++) {
						createVacationRequestForMonth(vacationRequesterIdx, year, month);
					}
				}
			}
		}
	}

	private void createVacationRequestForMonth(int vacationRequesterIdx, int year, int month) {
		VacationRequest vacationRequest = genVacationRequest(vacationRequesters.get(vacationRequesterIdx),
			YearMonth.of(year, month).atDay(1).atStartOfDay(),
			YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59), "testReason",
			vacationTypeCodes.get(vacationRequesterIdx),
			month == 1 ? VacationRequestStatus.APPROVED : VacationRequestStatus.IN_PROGRESS);
		vacationRequestRepository.save(vacationRequest);

		// 결재 단계 생성
		ApprovalStatus approvalStatus = (month == 1) ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;

		approvalStepRepository.save(
			genApprovalStep(1, approvalStatus, firstApprovers.get(vacationRequesterIdx), vacationRequest)
		);

		approvalStepRepository.save(
			genApprovalStep(2, approvalStatus, secondApprover, vacationRequest)
		);
	}

	@Test
	@DisplayName("검색 조건이 없을 때 모든 휴가 신청을 조회한다")
	void should_searchVacationRequests_when_defaultSearchCondition() {
		// given & when
		AdminVacationSearchCondition defaultSearchCondition = new AdminVacationSearchCondition(null, null, null);

		// then
		Page<VacationRequestSearchResponse> result = adminVacationRequestSearchCustom.search(
			defaultSearchCondition, pageable);
		assertThat(result).hasSize(TOTAL_VACATION_REQUESTS_CNT);
		assertThat(vacationRequestRepository.count()).isEqualTo(TOTAL_VACATION_REQUESTS_CNT);
	}

	@ParameterizedTest
	@MethodSource("validDataProvider")
	@DisplayName("유효한 검색 조건으로 휴가 신청을 조회한다")
	void should_successSearchVacationRequests_when_givenValidData(
		AdminVacationSearchCondition searchCondition, int expectedResult) {
		// then
		Page<VacationRequestSearchResponse> searchResult = adminVacationRequestSearchCustom.search(
			searchCondition, pageable);
		assertThat(searchResult).hasSize(expectedResult);
	}

	// 🔑 static 제거하고 인스턴스 메서드로 변경 (동적 ID 사용 가능)
	Stream<Arguments> validDataProvider() {
		return Stream.of(
			// 시작 년도 1월 검색
			Arguments.of(
				createByDateRange(LocalDate.of(START_YEAR, 1, 1), LocalDate.of(START_YEAR, 1, 31)),
				VACATION_REQUESTER_CNT * VACATION_REQUEST_CNT_PER_MONTH
			),
			// 시작 년도 전체 검색
			Arguments.of(
				createByDateRange(LocalDate.of(START_YEAR, 1, 1), LocalDate.of(START_YEAR, 12, 31)),
				VACATION_REQUESTER_CNT * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
			),
			// 시작 년도 상반기 검색
			Arguments.of(
				createByDateRange(START_YEAR, Quarter.H1),
				VACATION_REQUESTER_CNT * (END_OF_MONTH / 2)
			),
			// 신청자 A 검색
			Arguments.of(
				createByApplicant("A", null),
				YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
			),
			// 부서0 검색
			Arguments.of(
				createByApplicant(null, "부서0"),
				YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
			),
			// 직급0(직급 코드) codeId 검색
			Arguments.of(
				createByApplicant(positionCodes.getFirst().getId(), null),
				YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
			),
			// 휴가0(휴가 종류 코드) codeId 검색
			Arguments.of(
				createByApplicant(null, vacationTypeCodes.getFirst().getId()),
				YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
			)
		);
	}

	@ParameterizedTest
	@MethodSource("invalidDataProvider")
	@DisplayName("잘못된 검색 조건일 때 예외가 발생한다")
	void should_fail_when_givenInvalidData(Supplier<AdminVacationSearchCondition> supplier) {
		// when & then
		assertThatThrownBy(supplier::get)
			.isInstanceOf(BadRequestException.class);
	}

	static Stream<Arguments> invalidDataProvider() {
		return Stream.of(
			Arguments.of(
				// 시작일 누락
				(Supplier<AdminVacationSearchCondition>)() ->
					createByDateRange(null, LocalDate.of(START_YEAR, 1, 31))
			),
			Arguments.of(
				// 종료일 누락
				(Supplier<AdminVacationSearchCondition>)() ->
					createByDateRange(LocalDate.of(START_YEAR, 1, 1), null)
			)
		);
	}
}
