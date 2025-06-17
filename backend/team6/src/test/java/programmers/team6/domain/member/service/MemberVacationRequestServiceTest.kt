package programmers.team6.domain.member.service

import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.dto.response.ApprovalStepDetailUpdateResponse
import programmers.team6.domain.admin.dto.response.VacationRequestDetailReadResponse
import programmers.team6.domain.vacation.enums.ApprovalStatus
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.ForbiddenException
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.mock.VacationRequestReaderFake
import programmers.team6.support.VacationMother
import java.time.LocalDateTime
import java.util.List

/**
 * 성공 테스트에 기존의 오브젝트 마더 패턴을 적용하려했지만 merge과정의 conflict를 우려해서 우선 Low하게 테스트 진행,
 * 이후 마이그레이션 이후에 테스트 수정 필요
 * @author gunwoong
 */
internal class MemberVacationRequestServiceTest {

    private var readerFake: VacationRequestReaderFake = mockk<VacationRequestReaderFake>()

    @BeforeEach
    fun setUp() {
        readerFake = VacationRequestReaderFake()
    }

    @Test
    @DisplayName("휴가계 id와 유저 id 제공시, 휴가계 디테일 반환")
    fun should_selectVacationRequestDetailById_when_givenVacationRequestIdAndMemberId() {

        val defaultVacationDetail = VacationMother.defaultVacationDetail()

        val vacationRequestId = defaultVacationDetail.id

        val memberId = defaultVacationDetail.memberId

        // when
        readerFake.putVacationRequestDetail(
            vacationRequestId, defaultVacationDetail
        )

        val defaultApprovalStep = VacationMother.defaultApprovalStep()

        readerFake.putApprovalStep(vacationRequestId, listOf(defaultApprovalStep))

        val memberVacationRequestService = MemberVacationRequestService(readerFake)

        // then
        val result = memberVacationRequestService.selectVacationRequestDetailById(vacationRequestId, memberId)

        Assertions.assertThat(result).extracting(
            VacationRequestDetailReadResponse::id,
            VacationRequestDetailReadResponse::from,
            VacationRequestDetailReadResponse::to,
            VacationRequestDetailReadResponse::memberId,
            VacationRequestDetailReadResponse::name,
            VacationRequestDetailReadResponse::deptName,
            VacationRequestDetailReadResponse::position,
            VacationRequestDetailReadResponse::reason,
            VacationRequestDetailReadResponse::vacationType,
            VacationRequestDetailReadResponse::vacationRequestStatus
        ).containsExactly(vacationRequestId,
            defaultVacationDetail.from,
            defaultVacationDetail.to,
            memberId,
            defaultVacationDetail.name,
            defaultVacationDetail.deptName,
            defaultVacationDetail.position,
            defaultVacationDetail.reason,
            defaultVacationDetail.vacationType,
            defaultVacationDetail.vacationRequestStatus)

        Assertions.assertThat(result.approvalStepDetailUpdateResponses).hasSize(1)
    }

    @Test
    @DisplayName("존재하지 않는 VacationRequestId 제공시, NotFoundException 발생")
    fun should_throwForbiddenException_when_givenNotExistVacationRequestId() {
        // given & when
        val memberVacationRequestService = MemberVacationRequestService(readerFake)

        // then
        Assertions.assertThatThrownBy {
            memberVacationRequestService.selectVacationRequestDetailById(0L, 0L)
        }.isInstanceOf(NotFoundException::class.java)
            .hasMessage(NotFoundErrorCode.NOT_FOUND_VACATION_REQUEST.message)
    }

    @Test
    @DisplayName("찾으려는 VacationRequest의 ApprovalSteps가 존재하지 않을 경우, NotFoundException 발생")
    fun should_throwNotFoundException_when_vacationRequestHasEmptyApprovalSteps() {
        // given & when

        val defaultVacationDetail = VacationMother.defaultVacationDetail()

        val vacationRequestId = defaultVacationDetail.id

        readerFake.putVacationRequestDetail(vacationRequestId, defaultVacationDetail)

        val memberVacationRequestService = MemberVacationRequestService(readerFake)

        // then
        Assertions.assertThatThrownBy {
            memberVacationRequestService.selectVacationRequestDetailById(
                vacationRequestId,
                defaultVacationDetail.memberId
            )
        }.isInstanceOf(NotFoundException::class.java)
            .hasMessage(NotFoundErrorCode.NOT_FOUND_APPROVAL_STEP.message)
    }

    @Test
    @DisplayName("입력된 memberId와 조회된 memberId가 다를 경우, ForbiddenException 발생")
    fun should_throwForbiddenException_when_givenNotEqualMemberId() {
        val findMemberId = 1L

        val givenVacation = VacationMother.defaultVacationDetail()
        val findVacation = VacationMother.defaultVacationDetail(findMemberId)

        readerFake.putVacationRequestDetail(givenVacation.memberId, findVacation)

        readerFake.putApprovalStep(givenVacation.id, listOf(VacationMother.defaultApprovalStep()))

        val memberVacationRequestService = MemberVacationRequestService(readerFake)

        // then
        Assertions.assertThatThrownBy {
            memberVacationRequestService.selectVacationRequestDetailById(
                givenVacation.id,
                givenVacation.memberId
            )
        }.isInstanceOf(ForbiddenException::class.java)
    }
}