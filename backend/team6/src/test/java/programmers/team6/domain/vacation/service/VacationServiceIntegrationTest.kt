package programmers.team6.domain.vacation.service

import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestCode
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestDept
import programmers.team6.domain.admin.repository.AdminVacationRequestSearchTestDataFactory.genTestMember
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.admin.service.DeptService
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto
import programmers.team6.domain.vacation.dto.response.VacationCreateResponseDto
import programmers.team6.domain.vacation.dto.response.VacationRequestCalendarResponse
import programmers.team6.domain.vacation.dto.response.VacationUpdateResponseDto
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationRequest
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.domain.vacation.util.mapper.VacationMapper
import java.time.LocalDateTime
import java.util.function.Consumer

/**
 * VacationService의 코드가 리팩토링이 많이 필요해보임, 리팩토링후 해당 테스트는 쉽게 교체될것같기에 임시로 테스트를 작성하였음
 * @author gunwoong
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(
    VacationService::class,
    VacationMapper::class,
    ApprovalStepService::class,
    VacationInfoLogPublisher::class,
    DeptService::class,
    VacationRequestSearchRepository::class
)
internal class VacationServiceIntegrationTest {
    @Autowired
    lateinit var vacationInfoRepository: VacationInfoRepository

    @Autowired
    lateinit var vacationRequestRepository: VacationRequestRepository

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var codeRepository: CodeRepository

    @Autowired
    lateinit var deptRepository: DeptRepository

    @Autowired
    lateinit var vacationService: VacationService


    @Test
    fun should_findMyVacationInfo_when_givenMemberId() {
        // given & when
        val positionCode: Code = codeRepository.save(genTestCode("POSITION", "00", "name"))
        val dept: Dept = deptRepository.save(genTestDept("deptName"))
        val requester: Member = memberRepository.save(genTestMember("A", dept, positionCode))
        vacationInfoRepository.save(VacationInfo(100.0, 5.0, VacationCode.ANNUAL.code, requester.id))

        // then
        val response = vacationService.getMyVacationInfo(requester.id!!)
        response.totalCount shouldBe 100.0
        response.useCount shouldBe 5.0
        response.remainCount shouldBe (100.0 - 5.0)
    }

    @Test
    fun should_saveVacation_when_givenValidMemberIdAndVacationCreateRequestDto() {
        // given & when
        val vacationTypeCode: Code = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode: Code = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept: Dept = deptRepository.save(genTestDept("deptName"))
        val hrDept: Dept = deptRepository.save(genTestDept("인사팀"))

        val requester: Member = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover: Member = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover: Member = memberRepository.save(genTestMember("C", hrDept, positionCode))
        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id))

        val from = LocalDateTime.now().plusDays(1)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        // then
        val response = vacationService.requestVacation(
            requester.id!!,
            vacationCreateRequestDto
        )
        assertThat(response)
            .extracting(VacationCreateResponseDto::from, VacationCreateResponseDto::to)
            .containsExactly(from, to)
    }

    @Test
    fun should_updateVacation_when_givenValidMemberIdAndVacationRequestIdAndVacationUpdateRequestDto() {
        // given & when
        val vacationTypeCode: Code = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode: Code = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept: Dept = deptRepository.save(genTestDept("deptName"))
        val hrDept: Dept = deptRepository.save(genTestDept("인사팀"))

        val requester: Member = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover: Member = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover: Member = memberRepository.save(genTestMember("C", hrDept, positionCode))
        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save<VacationInfo?>(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id))

        val from = LocalDateTime.now().plusDays(1)
        val to = from.plusDays(1)

        val requestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        val response = vacationService.requestVacation(requester.id!!, requestDto)

        val updatedFrom = LocalDateTime.now().plusDays(5)
        val updatedTo = updatedFrom.plusDays(1)
        val updateRequestDto = VacationUpdateRequestDto(
            updatedFrom, updatedTo,
            "updatedReason",
            vacationTypeCode.code
        )

        // then
        val result = vacationService.updateVacationRequest(
            requester.id!!,
            response.requestId, updateRequestDto
        )

        assertThat<VacationUpdateResponseDto?>(result)
            .extracting(
                VacationUpdateResponseDto::requestId, VacationUpdateResponseDto::from,
                VacationUpdateResponseDto::to
            )
            .containsExactly(response.requestId, updatedFrom, updatedTo)
    }

    @Test
    fun should_cancelVacationRequest_when_givenMemberIdAndRequestId() {
        // given & when
        val vacationTypeCode: Code = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode: Code = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept: Dept = deptRepository.save(genTestDept("deptName"))
        val hrDept: Dept = deptRepository.save(genTestDept("인사팀"))

        val requester: Member = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover: Member = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover: Member = memberRepository.save(genTestMember("C", hrDept, positionCode))
        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save<VacationInfo?>(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id))

        val from = LocalDateTime.now().plusDays(1)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        val response = vacationService.requestVacation(
            requester.id!!,
            vacationCreateRequestDto
        )

        // then
        vacationService.cancelVacationRequest(requester.id!!, response.requestId)
        val result = vacationRequestRepository.findById(response.requestId)
        assertThat(result).isPresent()
        assertThat(result.get().status).isEqualTo(VacationRequestStatus.CANCELED)
    }

    @Test
    fun should_selectVacationCalendar_when_givenYearAndDeptId() {
        // given & when
        val vacationTypeCode: Code = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode: Code = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept: Dept = deptRepository.save(genTestDept("deptName"))
        val hrDept: Dept = deptRepository.save(genTestDept("인사팀"))

        val requester: Member = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover: Member = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover: Member = memberRepository.save(genTestMember("C", hrDept, positionCode))
        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save<VacationInfo?>(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id))

        val from = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        val response = vacationService.requestVacation(
            requester.id!!,
            vacationCreateRequestDto
        )
        vacationRequestRepository.findById(response.requestId)
            .ifPresent(Consumer { result: VacationRequest? -> result!!.approve() })

        // then
        val result: List<VacationRequestCalendarResponse> = vacationService.selectVacationCalendar(
            "2025-01", dept.id
        )
        assertThat<VacationRequestCalendarResponse?>(result).hasSize(1)
    }
}