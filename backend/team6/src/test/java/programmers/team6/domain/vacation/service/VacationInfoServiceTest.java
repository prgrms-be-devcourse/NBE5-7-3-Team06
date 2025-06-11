package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequest;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequests;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequestsList;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;

@ExtendWith(MockitoExtension.class)
class VacationInfoServiceTest {

	@Mock
	private VacationInfoRepository repository;

	@Mock
	private VacationInfoLogPublisher publisher;

	@Test
	void 휴가총합수정테스트() {
		VacationInfoService service = createService();
		VacationInfoUpdateTotalCountRequest vacationInfoUpdateTotalCountRequest1 = createVacationDto(1, 12, "01", 0);
		VacationInfoUpdateTotalCountRequest vacationInfoUpdateTotalCountRequest2 = createVacationDto(2, 13, "02", 0);
		VacationInfoUpdateTotalCountRequests request = createUpdateTotalCountRequest(
			vacationInfoUpdateTotalCountRequest1, vacationInfoUpdateTotalCountRequest2);
		VacationInfo vacationInfo1 = createVacationInfo(vacationInfoUpdateTotalCountRequest1);
		VacationInfo vacationInfo2 = createVacationInfo(vacationInfoUpdateTotalCountRequest2);
		when(repository.findAllByVacationIdIn(List.of(1, 2))).thenReturn(List.of(vacationInfo1, vacationInfo2));

		service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(request)));

		assertThat(vacationInfo1.getTotalCount()).isEqualTo(12);
		assertThat(vacationInfo2.getTotalCount()).isEqualTo(13);
	}

	private VacationInfoService createService() {
		return new VacationInfoService(repository, new VacationGrantRuleFinder(), publisher);
	}

	private VacationInfoUpdateTotalCountRequest createVacationDto(int id, double totalCount, String type, int version) {
		return new VacationInfoUpdateTotalCountRequest(id, totalCount, type, version);
	}

	private VacationInfoUpdateTotalCountRequests createUpdateTotalCountRequest(
		VacationInfoUpdateTotalCountRequest... vacationInfoUpdateTotalCountRequests) {
		return new VacationInfoUpdateTotalCountRequests(1L, List.of(vacationInfoUpdateTotalCountRequests));
	}

	private VacationInfo createVacationInfo(VacationInfoUpdateTotalCountRequest infoDto) {
		return new VacationInfo(infoDto.totalCount(), 0, infoDto.type(), 1L);
	}
}