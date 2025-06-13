package programmers.team6.domain.vacation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequest;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequests;
import programmers.team6.domain.admin.dto.VacationInfoUpdateTotalCountRequestsList;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.entity.VacationInfoLog;
import programmers.team6.domain.vacation.repository.VacationInfoRepository;
import programmers.team6.domain.vacation.rule.VacationGrantRule;
import programmers.team6.domain.vacation.rule.VacationGrantRuleFinder;
import programmers.team6.domain.vacation.support.VacationInfoLogPublisher;
import programmers.team6.global.exception.customException.BadRequestException;
import programmers.team6.global.exception.customException.ConflictException;

@ExtendWith(MockitoExtension.class)
class VacationInfoServiceTest {

	@Mock
	private VacationInfoRepository repository;

	@Mock
	private VacationGrantRuleFinder vacationGrantRuleFinder;

	@Mock
	private VacationInfoLogPublisher publisher;

	@Mock
	private VacationGrantRule vacationGrantRule;

	@InjectMocks
	private VacationInfoService service;

	private static final double TOTAL_COUNT_15 = 15.0;
	private static final double TOTAL_COUNT_MIN = 0.0;
	private static final double TOTAL_COUNT_MAX = 25.0;
	private static final double TOTAL_COUNT_OVER = 26.0;
	private static final double TOTAL_COUNT_NEGATIVE = -1.0;
	private static final String VACATION_TYPE_01 = "01";
	private static final String VACATION_TYPE_02 = "02";
	private static final Long MEMBER_ID_1 = 1L;
	private static final Long MEMBER_ID_2 = 2L;

	private VacationInfoUpdateTotalCountRequest createVacationDto(int id, double totalCount, String type, int version) {
		return new VacationInfoUpdateTotalCountRequest(id, totalCount, type, version);
	}

	private VacationInfoUpdateTotalCountRequests createUpdateTotalCountRequest(Long memberId,
		VacationInfoUpdateTotalCountRequest... requests) {
		return new VacationInfoUpdateTotalCountRequests(memberId, List.of(requests));
	}

	private VacationInfo createVacationInfo(int vacationId, double totalCount, String type, Long memberId) {
		return new VacationInfo(totalCount, 0, type, memberId);
	}

	@Nested
	@DisplayName("성공 테스트")
	class success_test {

		@Test
		@DisplayName("단일 직원의 여러 휴가 타입 업데이트")
		void updateFrom_single_member_success() {
			// given
			VacationInfoUpdateTotalCountRequest request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0);
			VacationInfoUpdateTotalCountRequest request2 = createVacationDto(2, TOTAL_COUNT_MIN, VACATION_TYPE_02, 0);
			VacationInfoUpdateTotalCountRequests memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request1, request2);

			VacationInfo vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);
			VacationInfo vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_02, MEMBER_ID_1);

			when(repository.findAllByVacationIdIn(List.of(1, 2))).thenReturn(List.of(vacationInfo1, vacationInfo2));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRuleFinder.find(VACATION_TYPE_02)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(anyDouble())).thenReturn(true);

			// when
			service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest)));

			// then
			assertThat(vacationInfo1.getTotalCount()).isEqualTo(TOTAL_COUNT_MAX);
			assertThat(vacationInfo2.getTotalCount()).isEqualTo(TOTAL_COUNT_MIN);
			verify(publisher, times(2)).publish(any(VacationInfoLog.class));
		}

		@Test
		@DisplayName("여러 직원의 휴가 정보 동시 업데이트")
		void updateFrom_multiple_members_success() {
			// given
			VacationInfoUpdateTotalCountRequest request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0);
			VacationInfoUpdateTotalCountRequest request2 = createVacationDto(2, TOTAL_COUNT_MIN, VACATION_TYPE_01, 0);

			VacationInfoUpdateTotalCountRequests memberRequest1 = createUpdateTotalCountRequest(MEMBER_ID_1, request1);
			VacationInfoUpdateTotalCountRequests memberRequest2 = createUpdateTotalCountRequest(MEMBER_ID_2, request2);

			VacationInfo vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);
			VacationInfo vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_2);

			when(repository.findAllByVacationIdIn(List.of(1, 2))).thenReturn(List.of(vacationInfo1, vacationInfo2));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(anyDouble())).thenReturn(true);

			// when
			service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest1, memberRequest2)));

			// then
			assertThat(vacationInfo1.getTotalCount()).isEqualTo(TOTAL_COUNT_MAX);
			assertThat(vacationInfo2.getTotalCount()).isEqualTo(TOTAL_COUNT_MIN);
			verify(publisher, times(2)).publish(any(VacationInfoLog.class));
		}

		@Test
		@DisplayName("부분 성공/실패")
		void updateFrom_partial() {
			// given
			VacationInfoUpdateTotalCountRequest request1 = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 0);
			VacationInfoUpdateTotalCountRequest request2 = createVacationDto(2, TOTAL_COUNT_OVER, VACATION_TYPE_01, 0);

			VacationInfoUpdateTotalCountRequests memberRequest1 = createUpdateTotalCountRequest(MEMBER_ID_1, request1);
			VacationInfoUpdateTotalCountRequests memberRequest2 = createUpdateTotalCountRequest(MEMBER_ID_2, request2);

			VacationInfo vacationInfo1 = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);
			VacationInfo vacationInfo2 = createVacationInfo(2, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_2);

			when(repository.findAllByVacationIdIn(List.of(1, 2))).thenReturn(List.of(vacationInfo1, vacationInfo2));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(TOTAL_COUNT_MAX)).thenReturn(true);
			when(vacationGrantRule.canUpdate(TOTAL_COUNT_OVER)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> service.updateFrom(
				new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest1, memberRequest2))))
				.isInstanceOf(BadRequestException.class);

			assertThat(vacationInfo1.getTotalCount()).isEqualTo(TOTAL_COUNT_MAX);
			assertThat(vacationInfo2.getTotalCount()).isEqualTo(TOTAL_COUNT_15);
			verify(publisher, times(1)).publish(any(VacationInfoLog.class));
		}

	}
	@Nested
	@DisplayName("실패 테스트")
	class failure_test {

		@Test
		@DisplayName("업데이트 한도 초과")
		void updateFrom_over_failure() {
			// given
			VacationInfoUpdateTotalCountRequest request = createVacationDto(1, TOTAL_COUNT_OVER, VACATION_TYPE_01, 0);
			VacationInfoUpdateTotalCountRequests memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request);
			VacationInfo vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);

			when(repository.findAllByVacationIdIn(List.of(1))).thenReturn(List.of(vacationInfo));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(TOTAL_COUNT_OVER)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest))))
				.isInstanceOf(BadRequestException.class);

			verify(publisher, never()).publish(any(VacationInfoLog.class));
		}

		@Test
		@DisplayName("음수 휴가일 업데이트")
		void updateFrom_negative_failure() {
			// given
			VacationInfoUpdateTotalCountRequest request = createVacationDto(1, TOTAL_COUNT_NEGATIVE, VACATION_TYPE_01, 0);
			VacationInfoUpdateTotalCountRequests memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request);
			VacationInfo vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);

			when(repository.findAllByVacationIdIn(List.of(1))).thenReturn(List.of(vacationInfo));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(TOTAL_COUNT_NEGATIVE)).thenReturn(false);

			// when & then
			assertThatThrownBy(() -> service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest))))
				.isInstanceOf(BadRequestException.class);

			verify(publisher, never()).publish(any(VacationInfoLog.class));
		}

		@Test
		@DisplayName("버전 불일치")
		void updateFrom_version_failure() {
			// given
			VacationInfoUpdateTotalCountRequest request = createVacationDto(1, TOTAL_COUNT_MAX, VACATION_TYPE_01, 1);
			VacationInfoUpdateTotalCountRequests memberRequest = createUpdateTotalCountRequest(MEMBER_ID_1, request);
			VacationInfo vacationInfo = createVacationInfo(1, TOTAL_COUNT_15, VACATION_TYPE_01, MEMBER_ID_1);

			when(repository.findAllByVacationIdIn(List.of(1))).thenReturn(List.of(vacationInfo));
			when(vacationGrantRuleFinder.find(VACATION_TYPE_01)).thenReturn(vacationGrantRule);
			when(vacationGrantRule.canUpdate(TOTAL_COUNT_MAX)).thenReturn(true);

			// when & then
			assertThatThrownBy(() -> service.updateFrom(new VacationInfoUpdateTotalCountRequestsList(List.of(memberRequest))))
				.isInstanceOf(ConflictException.class);

			verify(publisher, never()).publish(any(VacationInfoLog.class));
		}

	}

}