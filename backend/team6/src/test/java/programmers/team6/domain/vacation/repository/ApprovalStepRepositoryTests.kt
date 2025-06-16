package programmers.team6.domain.vacation.repository

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.response.ApprovalFirstStepSelectResponse
import programmers.team6.domain.vacation.dto.response.ApprovalSecondStepSelectResponse
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApprovalStepRepositoryTests {

    @Autowired
    private lateinit var approvalStepRepository: ApprovalStepRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var deptRepository: DeptRepository

    @Autowired
    private lateinit var codeRepository: CodeRepository

    @Autowired
    private lateinit var vacationRequestRepository: VacationRequestRepository

    private var memberId: Long = 0L
    private var approverId: Long = 0L
    private var approvalFirstStepId: Long = 0L
    private lateinit var savedVacation1: VacationRequest
    private lateinit var savedApprovalFirstStep: ApprovalStep
    private lateinit var savedApprovalSecondStep: ApprovalStep

    /**
     * 휴가 총 4개
     * 상태 : 승인 2, 반려 1, 대기 1
     * 날짜 : 8월 3, 9월 1
     */
    @BeforeAll
    fun setUp() {
        val savePosition01 = codeRepository.save(Code("POSITION", "01", "사원"))
        val savePosition04 = codeRepository.save(Code("POSITION", "04", "부장"))
        val saveVacationType01 = codeRepository.save(Code("VACATION_TYPE", "01", "연차"))
        val dept = deptRepository.save(Dept(deptName = "인사팀", deptLeader = null))

        val member = memberRepository.save(
            Member(
                name = "민경준",
                dept = dept,
                position = savePosition01,
                joinDate = LocalDateTime.of(2025, 1, 1, 0, 0),
                role = Role.USER
            )
        )

        val approver = memberRepository.save(
            Member(
                name = "홍길동",
                dept = dept,
                position = savePosition04,
                joinDate = LocalDateTime.of(2025, 1, 1, 0, 0),
                role = Role.USER
            )
        )

        memberId = member.id!!
        approverId = approver.id!!

        savedVacation1 = vacationRequestRepository.save(
            VacationRequest.create(
                member = member,
                from = LocalDateTime.of(2025, 8, 1, 9, 0),
                to = LocalDateTime.of(2025, 8, 3, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.APPROVED
            )
        )

        val savedVacation2 = vacationRequestRepository.save(
            VacationRequest.create(
                member = member,
                from = LocalDateTime.of(2025, 8, 10, 9, 0),
                to = LocalDateTime.of(2025, 8, 13, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.APPROVED
            )
        )

        val savedVacation3 = vacationRequestRepository.save(
            VacationRequest.create(
                member = member,
                from = LocalDateTime.of(2025, 8, 20, 9, 0),
                to = LocalDateTime.of(2025, 8, 21, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.REJECTED
            )
        )

        val savedVacation4 = vacationRequestRepository.save(
            VacationRequest.create(
                member = member,
                from = LocalDateTime.of(2025, 9, 1, 9, 0),
                to = LocalDateTime.of(2025, 9, 3, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.IN_PROGRESS
            )
        )

        savedApprovalFirstStep = approvalStepRepository.save(
            ApprovalStep(
                step = 1,
                approvalStatus = ApprovalStatus.APPROVED,
                member = approver,
                vacationRequest = savedVacation1
            )
        )

        approvalFirstStepId = savedApprovalFirstStep.id!!

        approvalStepRepository.save(
            ApprovalStep(
                step = 1,
                approvalStatus = ApprovalStatus.APPROVED,
                member = approver,
                vacationRequest = savedVacation2
            )
        )

        approvalStepRepository.save(
            ApprovalStep(
                step = 1,
                approvalStatus = ApprovalStatus.REJECTED,
                member = approver,
                vacationRequest = savedVacation3
            )
        )

        approvalStepRepository.save(
            ApprovalStep(
                step = 1,
                approvalStatus = ApprovalStatus.PENDING,
                member = approver,
                vacationRequest = savedVacation4
            )
        )

        savedApprovalSecondStep = approvalStepRepository.save(
            ApprovalStep(
                step = 2,
                approvalStatus = ApprovalStatus.APPROVED,
                member = approver,
                vacationRequest = savedVacation1
            )
        )

        approvalStepRepository.save(
            ApprovalStep(
                step = 2,
                approvalStatus = ApprovalStatus.APPROVED,
                member = approver,
                vacationRequest = savedVacation2
            )
        )

        approvalStepRepository.save(
            ApprovalStep(
                step = 2,
                approvalStatus = ApprovalStatus.REJECTED,
                member = approver,
                vacationRequest = savedVacation3
            )
        )

        approvalStepRepository.save(
            ApprovalStep(
                step = 2,
                approvalStatus = ApprovalStatus.WAITING,
                member = approver,
                vacationRequest = savedVacation4
            )
        )
    }

    @Test
    @DisplayName("1차 결재자 ID로 1차 결재 목록을 조회하면 목록이 반환된다")
    fun findFirstStep_test() {
        // given
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalFirstStep = approvalStepRepository.findFirstStepByMemberId(approverId, 1, pageable)

        // then
        assertThat(findApprovalFirstStep.content).hasSize(4)
    }

    @Test
    @DisplayName("1차 결재자 ID의 1차 결재 목록 중 이름에 민이 들어가고 8월에 승인된 목록은 2개일 것이다")
    fun findFirstStepByFilter_test() {
        // given
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalFirstStep = approvalStepRepository.findFirstStepByFilter(
            approverId, null, "민",
            LocalDateTime.of(2025, 8, 1, 0, 0),
            LocalDateTime.of(2025, 8, 31, 23, 59),
            ApprovalStatus.APPROVED, 1, pageable
        )

        // then
        assertThat(findApprovalFirstStep.content).hasSize(2)
    }

    @Test
    @DisplayName("2차 결재자 ID로 2차 결재 목록을 조회하면 목록을 반환된다")
    fun findSecondStep_test() {
        // given
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalSecondStep = approvalStepRepository.findSecondStepByMemberId(approverId, 2, pageable)

        // then
        assertThat(findApprovalSecondStep.content).hasSize(4)
    }

    @Test
    @DisplayName("2차 결재자 ID의 2차 결재 목록 중 이름에 민이 들어가고 8월에 거절된 목록은 1개일 것이다")
    fun findSecondStepByFilter_test() {
        // given
        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalSecondStep = approvalStepRepository.findSecondStepByFilter(
            approverId, null, "민",
            LocalDateTime.of(2025, 8, 1, 0, 0),
            LocalDateTime.of(2025, 8, 31, 23, 59),
            ApprovalStatus.REJECTED, 2, pageable
        )

        // then
        assertThat(findApprovalSecondStep.content).hasSize(1)
    }

    @Test
    @DisplayName("1차 결재 ID와 결재자 ID를 주면 1차 결재 정보를 반환할 것이다")
    fun findByIdAndMemberIdAndStep_test() {
        // when
        val findApproval = approvalStepRepository.findByIdAndMemberIdAndStep(
            approvalFirstStepId, approverId, 1
        )!!

        // then
        assertThat(findApproval.id).isEqualTo(savedApprovalFirstStep.id)
        assertThat(findApproval.step).isEqualTo(savedApprovalFirstStep.step)
        assertThat(findApproval.approvalStatus).isEqualTo(savedApprovalFirstStep.approvalStatus)
        assertThat(findApproval.member.id).isEqualTo(savedApprovalFirstStep.member.id)
        assertThat(findApproval.vacationRequest.id).isEqualTo(savedApprovalFirstStep.vacationRequest.id)
    }

    @Test
    @DisplayName("휴가 정보와 step 2를 주면 해당 휴가의 2차 결재 정보가 반환될 것이다")
    fun findByVacationRequestAndStep_test() {
        // when
        val findApproval = approvalStepRepository.findByVacationRequestAndStep(savedVacation1, 2)!!

        // then
        assertThat(findApproval.id).isEqualTo(savedApprovalSecondStep.id)
        assertThat(findApproval.step).isEqualTo(savedApprovalSecondStep.step)
        assertThat(findApproval.approvalStatus).isEqualTo(savedApprovalSecondStep.approvalStatus)
        assertThat(findApproval.member.id).isEqualTo(savedApprovalSecondStep.member.id)
        assertThat(findApproval.vacationRequest.id).isEqualTo(savedApprovalSecondStep.vacationRequest.id)
    }
}