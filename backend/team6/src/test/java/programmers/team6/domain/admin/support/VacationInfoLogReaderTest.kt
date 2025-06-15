package programmers.team6.domain.admin.support

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest
import programmers.team6.domain.vacation.entity.VacationInfoLog
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository
import java.time.LocalDateTime
import java.util.Map

internal class VacationInfoLogReaderTest {
    @Test
    fun 마지막로그검색() {
        val repository = mockk<VacationInfoLogRepository>()
        val log1 = VacationInfoLog(15.0, 10.0, "01", 1L, LocalDateTime.of(2024, 10, 18, 0, 0, 0))
        val log2 = VacationInfoLog(15.0, 11.0, "01", 1L, LocalDateTime.of(2024, 10, 19, 0, 0, 0))
        every {
            repository.findLastedByMemberIdInAndYear(
                listOf(1L),
                any(),
                "01"
            )
        } returns listOf(log1, log2)

        val reader = VacationInfoLogReader(repository)

        val vacationInfoLogs = reader.lastedLogsFrom(
            listOf(1L), VacationStatisticsRequest(
                2024, null, null, "01"
            )
        )

        assertThat(vacationInfoLogs).isEqualTo(VacationInfoLogs(Map.of(1L, log2)))
    }
}