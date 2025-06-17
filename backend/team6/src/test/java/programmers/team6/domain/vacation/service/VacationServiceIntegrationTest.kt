package programmers.team6.domain.vacation.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.admin.service.DeptService
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.dto.request.VacationUpdateRequestDto
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.enums.VacationCode
import programmers.team6.domain.vacation.enums.VacationRequestStatus
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.domain.vacation.util.mapper.VacationMapper
import java.time.LocalDateTime

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
@ActiveProfiles("test")
class VacationServiceIntegrationTest {

    @Autowired
    private lateinit var vacationService: VacationService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var codeRepository: CodeRepository

    @Autowired
    private lateinit var deptRepository: DeptRepository

    @Autowired
    private lateinit var vacationInfoRepository: VacationInfoRepository

    @Autowired
    private lateinit var vacationRequestRepository: VacationRequestRepository

    @Test
    fun `should find my vacation info when given member id`() {
        // given & when
        val positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"))
        val dept = deptRepository.save(genTestDept("deptName"))
        val requester = memberRepository.save(genTestMember("A", dept, positionCode))
        vacationInfoRepository.save(VacationInfo(100.0, 5.0, VacationCode.ANNUAL.code, requester.id!!))

        // then
        val response = vacationService.getMyVacationInfo(requester.id!!)
        assertThat(response)
            .extracting(
                { it.totalCount },
                { it.useCount },
                { it.remainCount }
            )
            .containsExactly(100.0, 5.0, 100.0 - 5.0)
    }

    @Test
    fun `should save vacation when given valid member id and vacation create request dto`() {
        // given & when
        val vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept = deptRepository.save(genTestDept("deptName"))
        val hrDept = deptRepository.save(genTestDept("인사팀"))

        val requester = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode))

        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id!!))

        val from = LocalDateTime.now().plusDays(1)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        // then
        val response = vacationService.requestVacation(requester.id!!, vacationCreateRequestDto)
        assertThat(response)
            .extracting(
                { it.from },
                { it.to }
            )
            .containsExactly(from, to)
    }

    @Test
    fun `should update vacation when given valid member id and vacation request id and vacation update request dto`() {
        // given & when
        val vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept = deptRepository.save(genTestDept("deptName"))
        val hrDept = deptRepository.save(genTestDept("인사팀"))

        val requester = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode))

        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id!!))

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
            updatedFrom,
            updatedTo,
            "updatedReason",
            vacationTypeCode.code
        )

        // then
        val result = vacationService.updateVacationRequest(
            requester.id!!,
            response.requestId,
            updateRequestDto
        )

        assertThat(result)
            .extracting(
                { it.requestId },
                { it.from },
                { it.to }
            )
            .containsExactly(response.requestId, updatedFrom, updatedTo)
    }

    @Test
    fun `should cancel vacation request when given member id and request id`() {
        // given & when
        val vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept = deptRepository.save(genTestDept("deptName"))
        val hrDept = deptRepository.save(genTestDept("인사팀"))

        val requester = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode))

        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id!!))

        val from = LocalDateTime.now().plusDays(1)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        val response = vacationService.requestVacation(requester.id!!, vacationCreateRequestDto)

        // then
        vacationService.cancelVacationRequest(requester.id!!, response.requestId)
        val result = vacationRequestRepository.findById(response.requestId)

        assertThat(result).isPresent
        assertThat(result.get().status).isEqualTo(VacationRequestStatus.CANCELED)
    }

    @Test
    fun `should select vacation calendar when given year and dept id`() {
        // given & when
        val vacationTypeCode = codeRepository.save(genTestCode("VACATION_TYPE", "00", "name"))
        val positionCode = codeRepository.save(genTestCode("POSITION", "00", "name"))

        val dept = deptRepository.save(genTestDept("deptName"))
        val hrDept = deptRepository.save(genTestDept("인사팀"))

        val requester = memberRepository.save(genTestMember("A", dept, positionCode))
        val firstApprover = memberRepository.save(genTestMember("B", dept, positionCode))
        val secondApprover = memberRepository.save(genTestMember("C", hrDept, positionCode))

        dept.appointLeader(firstApprover)
        hrDept.appointLeader(secondApprover)

        vacationInfoRepository.save(VacationInfo(100.0, 5.0, vacationTypeCode.code, requester.id!!))

        val from = LocalDateTime.of(2025, 1, 1, 0, 0, 0)
        val to = from.plusDays(1)
        val vacationCreateRequestDto = VacationCreateRequestDto(
            from,
            to,
            "reason",
            vacationTypeCode.code
        )

        val response = vacationService.requestVacation(requester.id!!, vacationCreateRequestDto)
        vacationRequestRepository.findById(response.requestId).ifPresent { result ->
            result.approve()
        }

        // then
        val result = vacationService.selectVacationCalendar("2025-01", dept.id)
        assertThat(result).hasSize(1)
    }

    // Helper methods
    private fun genTestCode(groupCode: String, code: String, name: String): Code {
        return Code(groupCode, code, name)
    }

    private fun genTestDept(deptName: String): Dept {
        return Dept(null, deptName, null)
    }

    private fun genTestMember(name: String, dept: Dept, position: Code): Member {
        return Member(
            name = name,
            dept = dept,
            position = position,
            joinDate = LocalDateTime.now(),
            role = Role.USER
        )
    }
}