package programmers.team6.domain.vacation.service

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Import
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringExtension
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.domain.vacation.dto.request.VacationCreateRequestDto
import programmers.team6.domain.vacation.repository.ApprovalStepRepository
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.repository.VacationRequestRepository
import programmers.team6.domain.vacation.repository.VacationRequestSearchRepository
import programmers.team6.domain.vacation.util.mapper.VacationMapper
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.NotFoundException
import programmers.team6.support.MemberMother
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@Import(VacationMapper::class)
class VacationServiceTests {

    private val vacationMapper: VacationMapper = mockk()
    private val vacationInfoRepository: VacationInfoRepository = mockk()
    private val vacationRequestRepository: VacationRequestRepository = mockk()
    private val approvalStepRepository: ApprovalStepRepository = mockk()
    private val memberRepository: MemberRepository = mockk()
    private val codeRepository: CodeRepository = mockk()
    private val approvalStepService: ApprovalStepService = mockk()
    private val vacationRequestSearchRepository: VacationRequestSearchRepository = mockk()

    private val vacationService = VacationService(
        vacationInfoRepository,
        vacationMapper,
        vacationRequestRepository,
        approvalStepRepository,
        memberRepository,
        codeRepository,
        approvalStepService,
        vacationRequestSearchRepository
    )

    @Nested
    inner class GetMyVacationInfo {
        @Test
        fun `should throw RuntimeException when given not exist member id`() {
            // when
            every { memberRepository.findById(0L) } returns Optional.empty()

            // then
            assertThatThrownBy { vacationService.getMyVacationInfo(0L) }
                .isInstanceOf(RuntimeException::class.java)
        }
    }

    @Nested
    inner class RequestVacation {
        @Test
        fun `should throw RuntimeException when given not exist vacation info`() {
            // when
            every { memberRepository.findByIdWithDeptAndLeader(0L) } returns null

            // then
            assertThatThrownBy { vacationService.requestVacation(0L, mockk()) }
                .isInstanceOf(RuntimeException::class.java)
        }

        @Test
        fun `should throw BadRequestException when given invalid from and to`() {
            // given
            val member = MemberMother.member()
            val from = LocalDateTime.now().plusDays(1)
            val to = LocalDateTime.now()
            val vacationCreateRequestDto = VacationCreateRequestDto(from, to, "empty", "empty")

            // when
            every { memberRepository.findByIdWithDeptAndLeader(0L) } returns member
            every {
                vacationRequestRepository.countInRangeFromBetweenToBy(member.id!!, from, to)
            } returns 1L

            // then
            assertThatThrownBy {
                vacationService.requestVacation(member.id!!, vacationCreateRequestDto)
            }.isInstanceOf(BadRequestException::class.java)
        }

        @Test
        fun `should throw NotFoundException when given invalid member id and vacation info type`() {
            // given
            val member = MemberMother.member()
            val from = LocalDateTime.now().plusDays(1)
            val to = LocalDateTime.now()
            val vacationType = "vacationType"
            val vacationCreateRequestDto = VacationCreateRequestDto(from, to, "reason", vacationType)

            // when
            every { memberRepository.findByIdWithDeptAndLeader(0L) } returns member
            every {
                vacationRequestRepository.countInRangeFromBetweenToBy(
                    member.id!!,
                    vacationCreateRequestDto.from,
                    vacationCreateRequestDto.to
                )
            } returns 0L
            every {
                vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)
            } returns 0.0
            every {
                vacationInfoRepository.findActualRemainingVacationDays(member.id!!, vacationType)
            } returns null

            // then
            assertThatThrownBy {
                vacationService.requestVacation(member.id!!, vacationCreateRequestDto)
            }.isInstanceOf(NotFoundException::class.java)
        }

        @Test
        fun `should throw BadRequestException when actual remain count less than request days`() {
            // given
            val member = MemberMother.member()
            val from = LocalDateTime.now().plusDays(1)
            val to = LocalDateTime.now()
            val vacationType = "vacationType"
            val vacationCreateRequestDto = VacationCreateRequestDto(from, to, "reason", vacationType)

            // when
            every { memberRepository.findByIdWithDeptAndLeader(0L) } returns member
            every {
                vacationRequestRepository.countInRangeFromBetweenToBy(
                    member.id!!,
                    vacationCreateRequestDto.from,
                    vacationCreateRequestDto.to
                )
            } returns 0L
            every {
                vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)
            } returns 5.0
            every {
                vacationInfoRepository.findActualRemainingVacationDays(member.id!!, vacationType)
            } returns 0.0

            // then
            assertThatThrownBy {
                vacationService.requestVacation(member.id!!, vacationCreateRequestDto)
            }.isInstanceOf(BadRequestException::class.java)
        }

        @Test
        fun `should throw RuntimeException when given invalid group code and code`() {
            // given
            val member = MemberMother.member()
            val leader = MemberMother.member()
            member.dept?.appointLeader(leader)

            val from = LocalDateTime.now().plusDays(1)
            val to = LocalDateTime.now()
            val vacationType = "vacationType"
            val vacationCreateRequestDto = VacationCreateRequestDto(from, to, "reason", vacationType)

            // when
            every { memberRepository.findByIdWithDeptAndLeader(0L) } returns member
            every {
                vacationRequestRepository.countInRangeFromBetweenToBy(
                    member.id!!,
                    vacationCreateRequestDto.from,
                    vacationCreateRequestDto.to
                )
            } returns 0L
            every {
                vacationRequestRepository.calculateRequestedVacationDays(from, to, vacationType)
            } returns 0.0
            every {
                vacationInfoRepository.findActualRemainingVacationDays(member.id!!, vacationType)
            } returns 1.0

            // then
            assertThatThrownBy {
                vacationService.requestVacation(member.id!!, vacationCreateRequestDto)
            }.isInstanceOf(RuntimeException::class.java)
        }
    }

    @Test
    fun `should save vacation when given valid member id and vacation create request dto`() {
        // given
        val member = MemberMother.member()
        val leader = MemberMother.member()
        member.dept?.appointLeader(leader)

        val from = LocalDateTime.now()
        val to = from.plusDays(2)
        val vacationCreateRequestDto = VacationCreateRequestDto(from, to, "reason", "vacationType")

        // when
        every { memberRepository.findByIdWithDeptAndLeader(member.id!!) } returns member
        every { vacationRequestRepository.countInRangeFromBetweenToBy(member.id!!, from, to) } returns 0L
        every {
            vacationRequestRepository.calculateRequestedVacationDays(
                from,
                to,
                vacationCreateRequestDto.vacationType
            )
        } returns Duration.between(from, to).toDays().toDouble()
        every {
            vacationInfoRepository.findActualRemainingVacationDays(
                member.id!!,
                vacationCreateRequestDto.vacationType
            )
        } returns 30.0
        every {
            codeRepository.findByGroupCodeAndCode("VACATION_TYPE", vacationCreateRequestDto.vacationType)
        } returns null

        // then
        assertThatThrownBy {
            vacationService.requestVacation(member.id!!, vacationCreateRequestDto)
        }.isInstanceOf(RuntimeException::class.java)
    }

    @Nested
    inner class GetVacationRequestList {
        @Test
        fun `should return empty result when find id page is empty`() {
            // given
            val member = MemberMother.member()

            // when
            every { memberRepository.findByIdOrNull(member.id!!) } returns member
            every {
                vacationRequestRepository.findIdsByRequesterIdPaging(any<Long>(), any<Pageable>())
            } returns Page.empty()

            // then
            val result = vacationService.getVacationRequestList(member.id!!, 0)
            assert(result.content.isEmpty())
            assert(result.pageNumber == 0)
        }
    }

    @Nested
    inner class UpdateVacationRequest {
    }
}