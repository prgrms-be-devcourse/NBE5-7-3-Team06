package programmers.team6.domain.vacation.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import jakarta.validation.Valid
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequest
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequests
import programmers.team6.domain.admin.dto.request.VacationInfoUpdateTotalCountRequestsList
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoRepository
import programmers.team6.domain.vacation.rule.VacationGrantRule
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher
import programmers.team6.global.exception.customException.BadRequestException
import programmers.team6.global.exception.customException.ConflictException
import java.util.List

private const val MEMBER_ID_1 = 1000L
private const val MEMBER_ID_2 = 2000L
private const val VACATION_TYPE_01 = "연차"
private const val VACATION_TYPE_02 = "병가"
private const val TOTAL_COUNT_15 = 15.0
private const val TOTAL_COUNT_MAX = 20.0
private const val TOTAL_COUNT_MIN = 5.0
private const val TOTAL_COUNT_OVER = 50.0
private const val TOTAL_COUNT_NEGATIVE = -5.0

@ExtendWith(MockKExtension::class)
internal class VacationInfoServiceTest {
    @MockK
    private lateinit var repository: VacationInfoRepository

    @MockK
    private lateinit var vacationGrantRuleFinder: VacationGrantRuleFinder

    @MockK
    private lateinit var publisher: VacationInfoLogPublisher

    @MockK
    private lateinit var vacationGrantRule: VacationGrantRule

    @InjectMockKs
    private lateinit var service: VacationInfoService

    private fun createVacationDto(
        id: Int,
        totalCount: Double,
        type: String,
        version: Int
    ): VacationInfoUpdateTotalCountRequest {
        return VacationInfoUpdateTotalCountRequest(id, totalCount, type, version)
    }

    private fun createUpdateTotalCountRequest(
        memberId: Long,
        vararg requests: VacationInfoUpdateTotalCountRequest
    ): VacationInfoUpdateTotalCountRequests {
        return VacationInfoUpdateTotalCountRequests(
            memberId,
            List.of<@Valid VacationInfoUpdateTotalCountRequest?>(*requests)
        )
    }

    private fun createVacationInfo(vacationId: Int, totalCount: Double, type: String, memberId: Long): VacationInfo {
        return VacationInfo(totalCount, 0.0, type, memberId)
    }

    @Nested
    @DisplayName("성공 테스트")
    internal inner class success_test {
        @Test
        @DisplayName("단일 직원의 여러 휴가 타입 업데이트")
        fun updateFrom_single_member_success() {
            // given
            val request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0)
            val request2 = createVacationDto(2, TOTAL_COUNT_MIN, VACATION_TYPE_02, 0)
            val memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request1, request2)

            val vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)
            val vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_02, MEMBER_ID_1)

            every { repository.findAllByVacationIdIn(listOf(1, 2)) } returns List.of(vacationInfo1, vacationInfo2)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRuleFinder.find(VACATION_TYPE_02) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(any()) } returns true
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when
            service.updateFrom(
                VacationInfoUpdateTotalCountRequestsList(
                    listOf(memberRequest)
                )
            )

            // then
            Assertions.assertThat(vacationInfo1.totalCount).isEqualTo(TOTAL_COUNT_MAX)
            Assertions.assertThat(vacationInfo2.totalCount).isEqualTo(TOTAL_COUNT_MIN)
            verify(exactly = 2) { publisher.publish(any<VacationInfoLog>()) }
        }

        @Test
        @DisplayName("여러 직원의 휴가 정보 동시 업데이트")
        fun updateFrom_multiple_members_success() {
            // given
            val request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0)
            val request2 = createVacationDto(2, TOTAL_COUNT_MIN, VACATION_TYPE_01, 0)

            val memberRequest1 = createUpdateTotalCountRequest(MEMBER_ID_1, request1)
            val memberRequest2 = createUpdateTotalCountRequest(MEMBER_ID_2, request2)

            val vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)
            val vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_2)

            every { repository.findAllByVacationIdIn(listOf(1, 2)) } returns List.of(vacationInfo1, vacationInfo2)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(any()) } returns true
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when
            service.updateFrom(
                VacationInfoUpdateTotalCountRequestsList(
                    listOf(memberRequest1, memberRequest2)
                )
            )

            // then
            Assertions.assertThat(vacationInfo1.totalCount).isEqualTo(TOTAL_COUNT_MAX)
            Assertions.assertThat(vacationInfo2.totalCount).isEqualTo(TOTAL_COUNT_MIN)
            verify(exactly = 2) { publisher.publish(any<VacationInfoLog>()) }
        }

        @Test
        @DisplayName("부분 성공/실패")
        fun updateFrom_partial() {
            // given
            val request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0)
            val request2 = createVacationDto(2, TOTAL_COUNT_OVER, VACATION_TYPE_01, 0)

            val memberRequest1 = createUpdateTotalCountRequest(MEMBER_ID_1, request1)
            val memberRequest2 = createUpdateTotalCountRequest(MEMBER_ID_2, request2)

            val vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)
            val vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_2)

            every { repository.findAllByVacationIdIn(listOf(1, 2)) } returns List.of(vacationInfo1, vacationInfo2)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(TOTAL_COUNT_MAX) } returns true
            every { vacationGrantRule.canUpdate(TOTAL_COUNT_OVER) } returns false
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when & then
            Assertions.assertThatThrownBy {
                service.updateFrom(
                    VacationInfoUpdateTotalCountRequestsList(
                        listOf(memberRequest1, memberRequest2)
                    )
                )
            }
                .isInstanceOf(BadRequestException::class.java)

            Assertions.assertThat(vacationInfo1.totalCount).isEqualTo(TOTAL_COUNT_MAX)
            Assertions.assertThat(vacationInfo2.totalCount).isEqualTo(TOTAL_COUNT_15)
            verify(exactly = 1) { publisher.publish(any<VacationInfoLog>()) }
        }
    }

    @Nested
    @DisplayName("실패 테스트")
    internal inner class failure_test {
        @Test
        @DisplayName("업데이트 한도 초과")
        fun updateFrom_over_failure() {
            // given
            val request = createVacationDto(1, TOTAL_COUNT_OVER, VACATION_TYPE_01, 0)
            val memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request)
            val vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)

            every { repository.findAllByVacationIdIn(listOf(1)) } returns List.of(vacationInfo)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(TOTAL_COUNT_OVER) } returns false
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when & then
            Assertions.assertThatThrownBy {
                service.updateFrom(
                    VacationInfoUpdateTotalCountRequestsList(
                        listOf(memberRequest)
                    )
                )
            }
                .isInstanceOf(BadRequestException::class.java)

            verify(exactly = 0) { publisher.publish(any<VacationInfoLog>()) }
        }

        @Test
        @DisplayName("음수 휴가일 업데이트")
        fun updateFrom_negative_failure() {
            // given
            val request = createVacationDto(1, TOTAL_COUNT_NEGATIVE, VACATION_TYPE_01, 0)
            val memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request)
            val vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)

            every { repository.findAllByVacationIdIn(listOf(1)) } returns List.of(vacationInfo)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(TOTAL_COUNT_NEGATIVE) } returns false
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when & then
            Assertions.assertThatThrownBy {
                service.updateFrom(
                    VacationInfoUpdateTotalCountRequestsList(
                        listOf(memberRequest)
                    )
                )
            }
                .isInstanceOf(BadRequestException::class.java)

            verify(exactly = 0) { publisher.publish(any<VacationInfoLog>()) }
        }

        @Test
        @DisplayName("버전 불일치")
        fun updateFrom_version_failure() {
            // given
            val request = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 1)
            val memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request)
            val vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1)

            every { repository.findAllByVacationIdIn(listOf(1)) } returns List.of(vacationInfo)
            every { vacationGrantRuleFinder.find(VACATION_TYPE_01) } returns vacationGrantRule
            every { vacationGrantRule.canUpdate(TOTAL_COUNT_MAX) } returns true
            justRun { publisher.publish(any<VacationInfoLog>()) }

            // when & then
            Assertions.assertThatThrownBy {
                service.updateFrom(
                    VacationInfoUpdateTotalCountRequestsList(
                        listOf(memberRequest)
                    )
                )
            }
                .isInstanceOf(ConflictException::class.java)

            verify(exactly = 0) { publisher.publish(any<VacationInfoLog>()) }
        }
    }
}
