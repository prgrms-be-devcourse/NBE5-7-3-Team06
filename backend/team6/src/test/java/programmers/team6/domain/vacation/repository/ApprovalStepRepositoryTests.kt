package programmers.team6.domain.vacation.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import java.time.LocalDateTime

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class ApprovalStepRepositoryTests @Autowired constructor(
    private val approvalStepRepository: ApprovalStepRepository,
    private val memberRepository: MemberRepository,
    private val deptRepository: DeptRepository,
    private val codeRepository: CodeRepository,
    private val vacationRequestRepository: VacationRequestRepository
) {

    private var approverId: Long = 0L
    private var approvalfirstStepId: Long = 0L
    private lateinit var savedVacation1: VacationRequest
    private lateinit var savedApprovalFirstStep: ApprovalStep
    private lateinit var savedApprovalSecondStep: ApprovalStep

    /**
     *
     * 휴가 총 4개
     * 상태 : 승인 2, 반려 1, 대기 1
     * 날짜 : 8월 3, 9월 1
     *
     */
    @BeforeEach
    fun setUp() {
        val savePosition01 = codeRepository.findByGroupCodeAndCode("POSITION", "01")
            ?: codeRepository.save(Code("POSITION", "01", "사원"))
        val savePosition04 = codeRepository.findByGroupCodeAndCode("POSITION", "04")
            ?: codeRepository.save(Code("POSITION", "04", "부장"))
        val saveVacationType01 = codeRepository.save(Code("VACATION_TYPE", "01", "연차"))
        val dept = deptRepository.save(Dept(null, "인사팀", null))
        val member = memberRepository.save(
            Member("민경준", dept, savePosition01, LocalDateTime.of(2025, 1, 1, 0, 0), Role.USER)
        )
        val approver = memberRepository.save(
            Member("홍길동", dept, savePosition04, LocalDateTime.of(2025, 1, 1, 0, 0), Role.USER)
        )
        approverId = approver.id!!
        savedVacation1 = vacationRequestRepository.save(
            VacationRequest(
                member = member,
                from = LocalDateTime.of(2025, 8, 1, 9, 0),
                to = LocalDateTime.of(2025, 8, 3, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.APPROVED
            )
        )


        val savedVacation2 = vacationRequestRepository.save(
            VacationRequest(
                member = member,
                from = LocalDateTime.of(2025, 8, 10, 9, 0),
                to = LocalDateTime.of(2025, 8, 13, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.APPROVED
            )
        )
        val savedVacation3 = vacationRequestRepository.save(
            VacationRequest(
                member = member,
                from = LocalDateTime.of(2025, 8, 20, 9, 0),
                to = LocalDateTime.of(2025, 8, 21, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.REJECTED
            )
        )
        val savedVacation4 = vacationRequestRepository.save(
            VacationRequest(
                member = member,
                from = LocalDateTime.of(2025, 9, 1, 9, 0),
                to = LocalDateTime.of(2025, 9, 3, 18, 0),
                reason = "사정이 있습니다.",
                type = saveVacationType01,
                status = VacationRequestStatus.IN_PROGRESS
            )
        )

        savedApprovalFirstStep = approvalStepRepository.save(
            ApprovalStep(null, approver, savedVacation1, ApprovalStatus.APPROVED, 1, null)
        )
        approvalfirstStepId = savedApprovalFirstStep.id!!
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation2, ApprovalStatus.APPROVED, 1, null))
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation3, ApprovalStatus.REJECTED, 1, null))
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation4, ApprovalStatus.PENDING, 1, null))
        savedApprovalSecondStep =
            approvalStepRepository.save(
                ApprovalStep(null, approver, savedVacation1, ApprovalStatus.APPROVED, 2, null)
            )
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation2, ApprovalStatus.APPROVED, 2, null))
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation3, ApprovalStatus.REJECTED, 2, null))
        approvalStepRepository.save(ApprovalStep(null, approver, savedVacation4, ApprovalStatus.WAITING, 2, null))
    }

    @Test
    @DisplayName("1차 결재자 ID로 1차 결재 목록을 조회하면 목록이 반환된다")
    fun findFirstStep_test() {
        // given

        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalFirstStep =
            approvalStepRepository.findFirstStepByMemberId(approverId, 1, pageable)

        // then
        Assertions.assertThat(findApprovalFirstStep.content).hasSize(4)
    }

    @Test
    @DisplayName("1차 결재자 ID의 1차 결재 목록 중 이름에 민이 들어가고 8월에 승인된 목록은 2개일 것이다")
    fun findFirstStepByFilter_test() {
        // given

        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalFirstStep = approvalStepRepository.findFirstStepByFilter(
            approverId, null, "민", LocalDateTime.of(2025, 8, 1, 0, 0), LocalDateTime.of(2025, 8, 31, 23, 59),
            ApprovalStatus.APPROVED, 1, pageable
        )

        // then
        Assertions.assertThat(findApprovalFirstStep.content).hasSize(2)
    }

    @Test
    @DisplayName("2차 결재자 ID로 2차 결재 목록을 조회하면 목록을 반환된다")
    fun findSecondStep_test() {
        // given

        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalSecondStep =
            approvalStepRepository.findSecondStepByMemberId(approverId, 1, pageable)

        // then
        Assertions.assertThat(findApprovalSecondStep.content).hasSize(4)
    }

    @Test
    @DisplayName("2차 결재자 ID의 1차 결재 목록 중 이름에 민이 들어가고 8월에 거절된 목록은 1개일 것이다")
    fun findSecondStepByFilter_test() {
        // given

        val pageable: Pageable = PageRequest.of(0, 10)

        // when
        val findApprovalSecondStep = approvalStepRepository.findSecondStepByFilter(
            approverId, null, "민", LocalDateTime.of(2025, 8, 1, 0, 0), LocalDateTime.of(2025, 8, 31, 23, 59),
            ApprovalStatus.REJECTED, 1, pageable
        )

        // then
        Assertions.assertThat(findApprovalSecondStep.content).hasSize(1)
    }

    @Test
    @DisplayName("1차 결재 ID와 결재자 ID를 주면 1차 결재 정보를 반환할 것이다")
    fun findByIdAndMemberIdAndStep_test() {
        // when

        val findApproval = approvalStepRepository.findByIdAndMemberIdAndStep(
            approvalfirstStepId, approverId, 1
        )

        // then
        Assertions.assertThat(findApproval!!.id).isEqualTo(savedApprovalFirstStep.id)
        Assertions.assertThat(findApproval.step).isEqualTo(savedApprovalFirstStep.step)
        Assertions.assertThat(findApproval.approvalStatus).isEqualTo(
            savedApprovalFirstStep.approvalStatus
        )
        Assertions.assertThat(findApproval.member.id).isEqualTo(savedApprovalFirstStep.member.id)
        Assertions.assertThat(findApproval.vacationRequest.id).isEqualTo(
            savedApprovalFirstStep.vacationRequest.id
        )
    }

    @Test
    @DisplayName("휴가 정보와 step 2를 주면 해당 휴가의 2차 결재 정보가 반환될 것이다")
    fun findByVacationRequestAndStep_test() {
        // when

        val findApproval = approvalStepRepository.findByVacationRequestAndStep(savedVacation1, 2)

        // then
        Assertions.assertThat(findApproval!!.id).isEqualTo(savedApprovalSecondStep.id)
        Assertions.assertThat(findApproval.step).isEqualTo(savedApprovalSecondStep.step)
        Assertions.assertThat(findApproval.approvalStatus).isEqualTo(
            savedApprovalSecondStep.approvalStatus
        )
        Assertions.assertThat(findApproval.member.id).isEqualTo(savedApprovalSecondStep.member.id)
        Assertions.assertThat(findApproval.vacationRequest.id).isEqualTo(
            savedApprovalSecondStep.vacationRequest.id
        )
    }
}