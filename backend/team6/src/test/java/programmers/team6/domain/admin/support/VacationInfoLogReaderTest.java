package programmers.team6.domain.admin.support;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import programmers.team6.domain.admin.dto.request.VacationStatisticsRequest;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.repository.VacationInfoLogRepository;

class VacationInfoLogReaderTest {

	@Test
	void 마지막로그검색() {
		VacationInfoLogRepository repository = Mockito.mock(VacationInfoLogRepository.class);
		VacationInfoLog log1 = new VacationInfoLog(15, 10, "01", 1L, LocalDateTime.of(2024, 10, 18, 0, 0, 0));
		VacationInfoLog log2 = new VacationInfoLog(15, 11, "01", 1L, LocalDateTime.of(2024, 10, 19, 0, 0, 0));
		when(repository.findLastedByMemberIdInAndYear(eq(List.of(1L)), any(LocalDateTime.class), eq("01")))
			.thenReturn(List.of(log1, log2));

		VacationInfoLogReader reader = new VacationInfoLogReader(repository);

		VacationInfoLogs vacationInfoLogs = reader.lastedLogsFrom(List.of(1L), new VacationStatisticsRequest(
			2024, null, null, "01"
		));

		assertThat(vacationInfoLogs).isEqualTo(new VacationInfoLogs(Map.of(1L, log2)));
	}

}