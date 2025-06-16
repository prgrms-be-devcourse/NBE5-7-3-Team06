package programmers.team6.domain.vacation.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import programmers.team6.domain.admin.service.DeptService
import programmers.team6.domain.vacation.dto.request.ApprovalStepRejectRequest
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.service.util.ApprovalStepServiceUtils.genFirstStep
import programmers.team6.domain.vacation.service.util.ApprovalStepServiceUtils.genSecondStep
import programmers.team6.domain.vacation.service.util.ApprovalStepServiceUtils.genVacationRequest
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.support.MemberMother

internal class ApprovalStepServiceTests {

    private val approvalStepRepository = mockk<ApprovalStepRepository>()

    private val vacationInfoRepository = mockk<VacationInfoRepository>()

    private val vacationInfoLogPublisher = mockk<VacationInfoLogPublisher>()

    private val deptService = mockk<DeptService>()

    private val approvalStepService =
        ApprovalStepService(approvalStepRepository, vacationInfoRepository, vacationInfoLogPublisher, deptService)

    @Test
    @DisplayName("1차 결재에서 1차 결재자가 인사담당자가 아닌 경우 승인 시 1차 APPROVED, 2차 PENDING")
    fun approveFirstStep_nonHrApprover_success() {
        // given
        val memberId = 1L
        val firstApproverId = 2L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val secondStepId = 2L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest)
        val secondApprovalStep: ApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep

        every { approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2) } returns secondApprovalStep

        // when
        approvalStepService.approveFirstStep(firstStepId, firstApproverId)

        //then
        AssertionsForClassTypes.assertThat(firstApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.APPROVED)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.PENDING)
    }

    @Test
    @DisplayName("1차 결재에서 1차 결재자가 인사담당자인 경우 승인 시 1차 APPROVED, 2차 APPROVED")
    fun approveFirstStep_HrApprover_success() {
        // given
        val memberId = 1L
        val hrApproverId = 2L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val hrApprover = MemberMother.withIdAndDeptName(hrApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)
        val vacationInfo = VacationInfo(15.0, "01", memberId)

        val firstStepId = 1L
        val secondStepId = 2L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, hrApprover, vacationRequest)
        val secondApprovalStep: ApprovalStep = genSecondStep(secondStepId, hrApprover, vacationRequest)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                hrApproverId,
                1
            )
        } returns firstApprovalStep
        every { approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2) } returns secondApprovalStep
        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                hrApproverId,
                2
            )
        } returns secondApprovalStep
        every { vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01") } returns vacationInfo
        every { vacationInfoLogPublisher.publish(any<VacationInfoLog>()) } just Runs

        // when
        approvalStepService.approveFirstStep(firstStepId, hrApproverId)

        //then
        AssertionsForClassTypes.assertThat(firstApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.APPROVED)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.APPROVED)
    }

    @Test
    @DisplayName("1차 승인에서 해당 1차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun approveFirstStep_findFirstStep_notFound_throwsNotFoundException() {
        // given
        val failedNum = 99999L
        val firstApproverId = 2L
        every { approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, firstApproverId, 1) } returns null

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveFirstStep(failedNum, firstApproverId)
        }.isInstanceOf(NotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("1차 승인에서 해당 1차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
    fun approveFirstStep_firstStepStatus_notPending_throwsBadRequestException(status: ApprovalStatus) {
        // given
        val memberId = 1L
        val firstApproverId = 2L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest, status)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveFirstStep(firstStepId, firstApproverId)
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    @DisplayName("1차 승인에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun approveFirstStep_findSecondStep_notFound_throwsNotFoundException() {
        // given
        val memberId = 1L
        val firstApproverId = 2L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep
        every { approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2) } returns null

        //then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveFirstStep(firstStepId, firstApproverId)
        }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("1차 결재에서 반려 시 1차 REJECTED, 2차 REJECTED, 휴가 REJECTED")
    fun rejectFirstStep_success() {
        // given
        val memberId = 1L
        val firstApproverId = 2L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val secondStepId = 2L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest)
        val secondApprovalStep: ApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep
        every { approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2) } returns secondApprovalStep

        // when
        approvalStepService.rejectFirstStep(firstStepId, firstApproverId, ApprovalStepRejectRequest("안됨"))

        //then
        AssertionsForClassTypes.assertThat(firstApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.REJECTED)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.REJECTED)
        AssertionsForClassTypes.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.REJECTED)
    }

    @Test
    @DisplayName("1차 반려에서 해당 1차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun rejectFirstStep_findFirstStep_notFound_throwsNotFoundException() {
        // given

        val failedNum = 99999L
        val firstApproverId = 2L

        every { approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, firstApproverId, 1) } returns null

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.rejectFirstStep(failedNum, firstApproverId, ApprovalStepRejectRequest("안됨"))
        }.isInstanceOf(NotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("1차 반려에서 해당 1차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
    fun rejectFirstStep_firstStepStatus_notPending_throwsBadRequestException(status: ApprovalStatus) {
        // given
        val memberId = 1L
        val firstApproverId = 2L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest, status)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.rejectFirstStep(firstStepId, firstApproverId, ApprovalStepRejectRequest("안됨"))
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    @DisplayName("1차 결재에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun rejectFirstStep_findSecondStep_notFound_throwsNotFoundException() {
        // given
        val memberId = 1L
        val firstApproverId = 2L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                firstStepId,
                firstApproverId,
                1
            )
        } returns firstApprovalStep
        every { approvalStepRepository.findByVacationRequestAndStep(vacationRequest, 2) } returns null

        //then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.rejectFirstStep(firstStepId, firstApproverId, ApprovalStepRejectRequest("안됨"))
        }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("2차 결재 승인 시 2차 APPROVED, 휴가 APPROVED, 휴가 일수 차감")
    fun approveSecondStep_success() {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)
        val vacationInfo = VacationInfo(15.0, "01", memberId)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genSecondStep(
            secondStepId, secondApprover, vacationRequest,
            ApprovalStatus.PENDING
        )

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep
        every { vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01") } returns vacationInfo
        every { vacationInfoLogPublisher.publish(any<VacationInfoLog>()) } just Runs

        // when
        val tf = approvalStepService.approveSecondStep(secondStepId, secondApproverId)

        //then
        AssertionsForClassTypes.assertThat(tf).isEqualTo(true)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.APPROVED)
        AssertionsForClassTypes.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.APPROVED)
        AssertionsForClassTypes.assertThat(vacationInfo.useCount).isEqualTo(3.0)
    }

    @Test
    @DisplayName("2차 승인에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun approveSecondStep_findSecondStep_notFound_throwsNotFoundException() {
        // given

        val failedNum = 99999L
        val secondApproverId = 2L

        every { approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, secondApproverId, 2) } returns null

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveSecondStep(failedNum, secondApproverId)
        }.isInstanceOf(NotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("2차 승인에서 해당 2차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
    fun approveSecondStep_secondStepStatus_notPending_throwsBadRequestException(status: ApprovalStatus) {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genFirstStep(secondStepId, secondApprover, vacationRequest, status)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveSecondStep(secondStepId, secondApproverId)
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    @DisplayName("2차 승인에서 휴가정보를 찾을 수 없는 경우 NotFoundException 예외 발생")
    fun approveSecondStep_findVacationInfo_notFound_throwsNotFoundException() {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genSecondStep(
            secondStepId, secondApprover, vacationRequest,
            ApprovalStatus.PENDING
        )

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep
        every { vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01") } returns null

        //then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.approveSecondStep(secondStepId, secondApproverId)
        }.isInstanceOf(NotFoundException::class.java)
    }

    @Test
    @DisplayName("2차 승인에서 총 휴가 일수를 넘긴 경우 2차 CANCELED, 휴가 CANCELED")
    fun approveSecondStep_cancel() {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)
        val vacationInfo = VacationInfo(15.0, 13.0, "01", memberId)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genSecondStep(
            secondStepId, secondApprover, vacationRequest,
            ApprovalStatus.PENDING
        )

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep
        every { vacationInfoRepository.findByMemberIdAndVacationType(memberId, "01") } returns vacationInfo

        // when
        val tf = approvalStepService.approveSecondStep(secondStepId, secondApproverId)

        //then
        AssertionsForClassTypes.assertThat(tf).isEqualTo(false)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.CANCELED)
        AssertionsForClassTypes.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.CANCELED)
    }

    @Test
    @DisplayName("2차 결재 반려 시 2차 REJECTED, 휴가 REJECTED")
    fun rejectSecondStep_success() {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genSecondStep(
            secondStepId, secondApprover, vacationRequest,
            ApprovalStatus.PENDING
        )

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep

        // when
        approvalStepService.rejectSecondStep(secondStepId, secondApproverId, ApprovalStepRejectRequest("안됨"))

        //then
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.REJECTED)
        AssertionsForClassTypes.assertThat(vacationRequest.status).isEqualTo(VacationRequestStatus.REJECTED)
    }

    @Test
    @DisplayName("2차 반려에서 해당 2차 결재를 찾을 수 없는 경우 NotFoundException 예외가 발생")
    fun rejectSecondStep_findSecondStep_notFound_throwsNotFoundException() {
        // given

        val failedNum = 99999L
        val secondApproverId = 2L

        every { approvalStepRepository.findByIdAndMemberIdAndStep(failedNum, secondApproverId, 2) } returns null

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.rejectSecondStep(failedNum, secondApproverId, ApprovalStepRejectRequest("안됨"))
        }.isInstanceOf(NotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["PENDING"])
    @DisplayName("2차 반려에서 해당 2차 결재상태가 PENDING이 아니면 BadRequestException 예외가 발생")
    fun rejectSecondStep_secondStepStatus_notPending_throwsBadRequestException(status: ApprovalStatus) {
        // given
        val memberId = 1L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val secondStepId = 2L
        val secondApprovalStep: ApprovalStep = genFirstStep(secondStepId, secondApprover, vacationRequest, status)

        every {
            approvalStepRepository.findByIdAndMemberIdAndStep(
                secondStepId,
                secondApproverId,
                2
            )
        } returns secondApprovalStep

        // then
        AssertionsForClassTypes.assertThatThrownBy {
            approvalStepService.rejectSecondStep(
                secondStepId, secondApproverId,
                ApprovalStepRejectRequest("안됨")
            )
        }.isInstanceOf(BadRequestException::class.java)
    }

    @Test
    @DisplayName("휴가 취소 시 1차 CANCELED, 2차 CANCELED")
    fun cancelApprovalStep_success() {
        // given
        val memberId = 1L
        val firstApproverId = 2L
        val secondApproverId = 3L

        val member = MemberMother.withIdAndDeptName(memberId, "개발팀")
        val firstApprover = MemberMother.withIdAndDeptName(firstApproverId, "개발팀")
        val secondApprover = MemberMother.withIdAndDeptName(secondApproverId, "인사팀")

        val vacationRequest: VacationRequest = genVacationRequest(member)

        val firstStepId = 1L
        val secondStepId = 2L
        val firstApprovalStep: ApprovalStep = genFirstStep(firstStepId, firstApprover, vacationRequest)
        val secondApprovalStep: ApprovalStep = genSecondStep(secondStepId, secondApprover, vacationRequest)

        every {
            approvalStepRepository.findByVacationRequest(vacationRequest)
        } returns listOf(
            firstApprovalStep,
            secondApprovalStep
        )

        // when
        approvalStepService.cancelApprovalStep(vacationRequest)

        //then
        AssertionsForClassTypes.assertThat(firstApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.CANCELED)
        AssertionsForClassTypes.assertThat(secondApprovalStep.approvalStatus).isEqualTo(ApprovalStatus.CANCELED)
    }
}