package programmers.team6.domain.admin.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.dto.request.VacationRequestDetailUpdateRequest
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchCustom
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.vacation.entity.ApprovalStep
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.support.VacationRequestReader
import programmers.team6.global.exception.code.ConflictErrorCode
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.ConflictException
import programmers.team6.global.exception.customException.NotFoundException
import java.time.LocalDateTime
import java.util.*

internal class AdminServiceTest {
    val adminVacationRequestSearchCustom = mockk<AdminVacationRequestSearchCustom>()
    val vacationRequestRepository = mockk<VacationRequestRepository>()
    val codeRepository = mockk<CodeRepository>()
    val approvalStepRepository = mockk<ApprovalStepRepository>()
    val vacationRequestReader = mockk<VacationRequestReader>()
    var adminService: AdminService = AdminService(
        adminVacationRequestSearchCustom,
        vacationRequestRepository,
        codeRepository,
        approvalStepRepository,
        vacationRequestReader
    )

    @Nested
    @DisplayName("VacationRequestDetail 수정 과정에서 ")
    internal inner class should_updateVacationRequestDetail {
        lateinit var vacationRequest: VacationRequest
        lateinit var vacationRequestType: Code
        lateinit var vacationRequestDetailUpdateRequest: VacationRequestDetailUpdateRequest
        lateinit var approvalSteps: MutableList<ApprovalStep>

        @BeforeEach
        fun setUp() {
            this.vacationRequest = VacationRequest(
                mockk<Member>(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(1), "", mockk<Code>(),
                VacationRequestStatus.IN_PROGRESS, 0
            )
            this.vacationRequestType = Code("VACATION_TYPE", UUID.randomUUID().toString(), "test_name")
            this.vacationRequestDetailUpdateRequest = VacationRequestDetailUpdateRequest(
                0L,
                LocalDateTime.now().plusDays(1L), LocalDateTime.now().plusDays(3L), VacationRequestStatus.APPROVED,
                "testReason", mutableListOf("r1", "r2", "r3")
            )
            this.approvalSteps = mutableListOf()
            for (i in 0..2) {
                approvalSteps.add(
                    AdminVacationRequestSearchTestDataFactory.genTestApprovalStep(
                        vacationRequest, i,
                        String.format("o%d", i)
                    )
                )
            }
        }

        @Test
        @DisplayName("알맞은 VacationRequestId와 VacationRequestDetail 입력시, update 성공")
        fun success_when_givenValidVacationRequestIdAndVacationRequestDetail() {
            // given
            val vacationRequestId = 0L

            // when
            every { vacationRequestRepository.findVacationRequestById(vacationRequestId) }.returns(vacationRequest)
            every {
                codeRepository.findByIdAndGroupCode(
                    vacationRequestDetailUpdateRequest.typeId,
                    "VACATION_TYPE"
                )
            }.returns(vacationRequestType)
            every { approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(vacationRequestId) }.returns(
                approvalSteps
            )
            adminService.updateVacationRequestDetailById(vacationRequestId, vacationRequestDetailUpdateRequest)

            // then
            vacationRequest.from shouldBe vacationRequestDetailUpdateRequest.from
            vacationRequest.to shouldBe vacationRequestDetailUpdateRequest.to
            vacationRequest.status shouldBe vacationRequestDetailUpdateRequest.vacationRequestStatus
            vacationRequest.reason shouldBe vacationRequestDetailUpdateRequest.reason

            assertThat(approvalSteps).hasSize(3)
                .extracting<String?, RuntimeException?>(ThrowingExtractor { obj: ApprovalStep? -> obj!!.getReason() })
                .containsExactly("r1", "r2", "r3")
        }

        @Test
        @DisplayName("잘못된 VacationRequestId 입력시, NotFoundException 발생")
        fun fail_when_givenInvalidVacationRequestId() {
            // given
            val vacationRequestId = 0L

            // when
            every { vacationRequestRepository.findVacationRequestById(vacationRequestId) }.returns(null)

            // then
            assertThatThrownBy({
                adminService.updateVacationRequestDetailById(
                    vacationRequestId,
                    vacationRequestDetailUpdateRequest
                )
            }).isInstanceOf(NotFoundException::class.java)
                .hasMessage(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST.getMessage())
        }

        @Test
        @DisplayName("잘못된 VacationRequest의 typeId(분류코드 id) 입력시, NotFoundException 발생")
        fun fail_when_givenInvalidVacationRequestTypeId() {
            // given
            val vacationRequestId = 0L

            // when
            every { vacationRequestRepository.findVacationRequestById(vacationRequestId) }.returns(vacationRequest)
            every {
                codeRepository.findByIdAndGroupCode(
                    vacationRequestDetailUpdateRequest.typeId,
                    "VACATION_TYPE"
                )
            }.returns(null)

            // then
            assertThatThrownBy({
                adminService.updateVacationRequestDetailById(
                    vacationRequestId,
                    vacationRequestDetailUpdateRequest
                )
            }).isInstanceOf(NotFoundException::class.java)
                .hasMessage(NotFoundErrorCode.NOT_FOUND_CODE.getMessage())
        }

        @Test
        @DisplayName("해당 VacationReuqest의 ApprovalStep이 없을 경우, ConflictException 발생")
        fun fail_when_givenEmptyApprovalSteps() {
            // given
            val vacationRequestId = 0L

            // when
            every { vacationRequestRepository.findVacationRequestById(vacationRequestId) }.returns(vacationRequest)
            every {
                codeRepository.findByIdAndGroupCode(
                    vacationRequestDetailUpdateRequest.typeId,
                    "VACATION_TYPE"
                )
            }.returns(vacationRequestType)
            every { approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(vacationRequestId) }.returns(
                emptyList()
            )

            // then
            assertThatThrownBy({
                adminService.updateVacationRequestDetailById(
                    vacationRequestId,
                    vacationRequestDetailUpdateRequest
                )
            }).isInstanceOf(ConflictException::class.java)
                .hasMessage(ConflictErrorCode.CONFLICT_APPROVAL_STEP.getMessage())
        }

        @Test
        @DisplayName("해당 VacationReuqest와 ApprovalStep가 동기화가 안된경우, ConflictException 발생")
        fun fail_when_givenInvalidApprovalSteps() {
            // given
            val vacationRequestId = 0L

            // when
            every { vacationRequestRepository.findVacationRequestById(vacationRequestId) }.returns(vacationRequest)
            every {
                codeRepository.findByIdAndGroupCode(
                    vacationRequestDetailUpdateRequest.typeId,
                    "VACATION_TYPE"
                )
            }.returns(vacationRequestType)
            every { approvalStepRepository.findApprovalStepsByVacationRequest_IdOrderByStepAsc(vacationRequestId) }.returns(
                listOf(mockk<ApprovalStep>())
            )

            // then
            assertThatThrownBy( {
                adminService.updateVacationRequestDetailById(
                    0L,
                    vacationRequestDetailUpdateRequest
                )
            }).isInstanceOf(ConflictException::class.java)
                .hasMessage(ConflictErrorCode.CONFLICT_APPROVAL_STEP.getMessage())
        }
    }
}