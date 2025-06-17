package programmers.team6.domain.admin.repository

import io.kotest.matchers.collections.shouldHaveSize
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.test.context.ActiveProfiles
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.DEFAULT_APPLICANT
import programmers.team6.domain.admin.dto.response.AdminVacationSearchCondition.Companion.DEFAULT_DATE_RANGE
import programmers.team6.domain.admin.dto.response.VacationRequestSearchResponse
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.enums.Quarter
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genApprovalStep
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestCodeList
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestDeptList
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestMember
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestMemberList
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genVacationRequest
import programmers.team6.domain.admin.repository.TestVacationRequestSearchConditionFactory.createByApplicant
import programmers.team6.domain.admin.repository.TestVacationRequestSearchConditionFactory.createByDateRange
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.global.exception.customException.BadRequestException
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.util.*
import java.util.stream.Stream

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AdminVacationRequestSearchCustom::class)
internal class AdminVacationRequestSearchCustomTest {
    @Autowired
    lateinit var adminVacationRequestSearchCustom: AdminVacationRequestSearchCustom

    @Autowired
    lateinit var vacationRequestRepository: VacationRequestRepository

    @Autowired
    lateinit var approvalStepRepository: ApprovalStepRepository

    @Autowired
    lateinit var codeRepository: CodeRepository

    @Autowired
    lateinit var deptRepository: DeptRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    private val pageable: Pageable = PageRequest.of(0, Int.Companion.MAX_VALUE)

    lateinit var vacationTypeCodes: List<Code>
    lateinit var positionCodes: List<Code>
    lateinit var depts: List<Dept>
    lateinit var vacationRequesters: List<Member>
    lateinit var firstApprovers: List<Member>
    lateinit var secondApprover: Member

    var beforeCnt = 0

    @BeforeEach
    fun setUpOnce() {
        beforeCnt += vacationRequestRepository.count().toInt()
        setupCodes()
        setupDepartments()
        setupMembers()
        setupVacationRequests()
    }

    private fun setupCodes() {
        // 휴가 타입 코드
        vacationTypeCodes = genTestCodeList(
            UUID.randomUUID().toString(),
            VACATION_TYPE_CNT,
            PREFIX_VACATION_TYPE
        )
        codeRepository.saveAll(vacationTypeCodes)

        // 부서 코드
        codeRepository.saveAll(
            genTestCodeList(
                UUID.randomUUID().toString(), DEPT_CNT, PREFIX_DEPT
            )
        )

        // 직급 코드
        positionCodes = genTestCodeList(
            UUID.randomUUID().toString(),
            POSITION_CNT,
            PREFIX_POSITION
        )
        codeRepository.saveAll(positionCodes)
    }

    private fun setupDepartments() {
        depts = genTestDeptList(DEPT_CNT, PREFIX_DEPT)
        deptRepository.saveAll(depts)
    }

    private fun setupMembers() {
        // 2차 결재자
        secondApprover = genTestMember(
            "D", depts.get(0), positionCodes.get(
                SECOUND_APPROVER_POSITION_IDX
            )
        )
        memberRepository.save(secondApprover)

        // 1차 결재자들
        firstApprovers = genTestMemberList(
            DEPT_CNT, "D", depts.subList(0, DEPT_CNT),
            positionCodes.get(FIRST_APPROVER_POSITION_IDX)
        )
        memberRepository.saveAll(firstApprovers)

        // 휴가 신청자들
        vacationRequesters = genTestMemberList(
            VACATION_REQUESTER_CNT, 'A', depts.subList(0, DEPT_CNT),
            positionCodes.subList(0, VACATION_REQUESTER_CNT)
        )
        memberRepository.saveAll(vacationRequesters)

        // 부서 리더 지정
        depts.zip(firstApprovers).forEach { (dept, approver) -> dept.appointLeader(approver) }
    }

    private fun setupVacationRequests() {
        for (vacationRequesterIdx in 0..<VACATION_REQUESTER_CNT) {
            for (year in START_YEAR..END_YEAR) {
                for (month in 1..END_OF_MONTH) {
                    repeat(VACATION_REQUEST_CNT_PER_MONTH) {
                        createVacationRequestForMonth(vacationRequesterIdx, year, month)
                    }
                }
            }
        }
    }

    private fun createVacationRequestForMonth(vacationRequesterIdx: Int, year: Int, month: Int) {
        val vacationRequest = vacationRequestRepository.save(
            genVacationRequest(
                vacationRequesters.get(vacationRequesterIdx),
                YearMonth.of(year, month).atDay(1).atStartOfDay(),
                YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59), "testReason",
                vacationTypeCodes.get(vacationRequesterIdx),
                if (month == 1) VacationRequestStatus.APPROVED else VacationRequestStatus.IN_PROGRESS
            )
        )

        // 결재 단계 생성
        val approvalStatus = if (month == 1) ApprovalStatus.APPROVED else ApprovalStatus.PENDING

        approvalStepRepository.save(
            genApprovalStep(
                1,
                approvalStatus,
                firstApprovers.get(vacationRequesterIdx),
                vacationRequest
            )
        )

        approvalStepRepository.save(
            genApprovalStep(
                2,
                approvalStatus,
                secondApprover,
                vacationRequest
            )
        )
    }

    @Test
    @DisplayName("검색 조건이 없을 때 모든 휴가 신청을 조회한다")
    fun should_searchVacationRequests_when_defaultSearchCondition() {
        // given & when
        val defaultSearchCondition = AdminVacationSearchCondition(
            DEFAULT_DATE_RANGE,
            DEFAULT_APPLICANT, null
        )

        // then
        val result: Page<VacationRequestSearchResponse> = adminVacationRequestSearchCustom.search(
            defaultSearchCondition, pageable
        )
        assertThat(result).hasSize(beforeCnt + TOTAL_VACATION_REQUESTS_CNT)
    }

    @ParameterizedTest
    @MethodSource("validDataProvider")
    @DisplayName("유효한 검색 조건으로 휴가 신청을 조회한다")
    fun should_successSearchVacationRequests_when_givenValidData(
        searchCondition: AdminVacationSearchCondition, expectedResult: Int
    ) {
        // then
        val searchResult: Page<VacationRequestSearchResponse> = adminVacationRequestSearchCustom.search(
            searchCondition, pageable
        )
        assertThat(searchResult).hasSize(expectedResult)
    }

    @Test
    @DisplayName("codeId 검색시, 해당 vacation request들 조회")
    fun should_successSearchVacationRequests_when_givenCodeId() {
        // given
        val positionCodeId = positionCodes.first().id!!
        val vacationTypeCodeId = vacationTypeCodes.first().id!!

        println("gwj $positionCodeId $vacationTypeCodeId")
        for (request in vacationRequestRepository.findAll()) {
            println("${request.id} ${request.member.position.id} ${request.type.id}")
        }
        // when & then
        val resultByPositionCodeId = adminVacationRequestSearchCustom.search(createByApplicant(positionCodeId, null), pageable)
        val resultByVacationTypeCodeId = adminVacationRequestSearchCustom.search(createByApplicant(null,vacationTypeCodeId), pageable)

        resultByPositionCodeId shouldHaveSize YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
        resultByVacationTypeCodeId shouldHaveSize YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH

    }


    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    @DisplayName("잘못된 검색 조건일 때 예외가 발생한다")
    fun should_fail_when_givenInvalidData(testCondition: () -> AdminVacationSearchCondition) {
        // when & then
        assertThatThrownBy { testCondition.invoke() }
            .isInstanceOf(BadRequestException::class.java)
    }

    companion object {
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
        private const val VACATION_REQUESTER_CNT = 3
        private val START_YEAR = Year.now().getValue() + 1
        private val END_YEAR: Int = START_YEAR + 1
        private val YEAR_DURATION: Int = END_YEAR - START_YEAR + 1
        private const val END_OF_MONTH = 12
        private const val VACATION_REQUEST_CNT_PER_MONTH = 1

        private const val VACATION_TYPE_CNT = 3
        private const val DEPT_CNT = 3
        private const val POSITION_CNT = 5
        private const val FIRST_APPROVER_POSITION_IDX = 3
        private const val SECOUND_APPROVER_POSITION_IDX = 4
        private val TOTAL_VACATION_REQUESTS_CNT: Int =
            VACATION_REQUESTER_CNT * YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH

        private const val PREFIX_VACATION_TYPE = "휴가"
        private const val PREFIX_DEPT = "부서"
        private const val PREFIX_POSITION = "직급"

        @JvmStatic
        fun validDataProvider(): Stream<Arguments> {
            return listOf(
                Arguments.of(
                    createByDateRange(LocalDate.of(START_YEAR, 1, 1), LocalDate.of(START_YEAR, 1, 31)),
                    VACATION_REQUESTER_CNT * VACATION_REQUEST_CNT_PER_MONTH
                ),  // 시작 년도 전체 검색
                Arguments.of(
                    createByDateRange(LocalDate.of(START_YEAR, 1, 1), LocalDate.of(START_YEAR, 12, 31)),
                    VACATION_REQUESTER_CNT * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
                ),  // 시작 년도 상반기 검색
                Arguments.of(
                    createByDateRange(START_YEAR, Quarter.H1),
                    VACATION_REQUESTER_CNT * (END_OF_MONTH / 2)
                ),  // 신청자 A 검색
                Arguments.of(
                    createByApplicant("A", null),
                    YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
                ),  // 부서0 검색
                Arguments.of(
                    createByApplicant(null, "부서0"),
                    YEAR_DURATION * END_OF_MONTH * VACATION_REQUEST_CNT_PER_MONTH
                ),
            ).stream()
        }

        @JvmStatic
        fun invalidDataProvider(): Stream<Arguments> {
            return listOf(
                Arguments.of( // 시작일 누락
                    { createByDateRange(null, LocalDate.of(START_YEAR, 1, 31)) }
                ),
                Arguments.of(
                    { createByDateRange(LocalDate.of(START_YEAR, 1, 1), null) }
                )
            ).stream()
        }
    }
}
