package programmers.team6.domain.vacation.repository;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.vacation.entity.VacationInfo;
import programmers.team6.domain.vacation.repository.factory.TestMemberFactory;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Transactional
@Import(value = TestMemberFactory.class)
class VacationInfoRepositoryTest {

	@Autowired
	private VacationInfoRepository vacationInfoRepository;
	@Autowired
	private TestMemberFactory memberFactory;

	@ParameterizedTest
	@CsvSource(value = {"2026-10-31,1", "2026-10-30,0"}, delimiter = ',')
	void 연차대상자_검색(LocalDate now, int count) {
		Member member = memberFactory.defaultMember();
		VacationInfo info = vacationInfoRepository.save(new VacationInfo(15, 0, "testType", member.getId()));

		List<VacationInfo> result = vacationInfoRepository.findAnnualVacationFrom(now.minusYears(1), now);

		assertThat(result).hasSize(count);
		if (count == 1) {
			assertThat(info).isEqualTo(result.getFirst());
		}
	}

	@ParameterizedTest
	@CsvSource(value = {"2026-02-28,1", "2026-05-31,1", "2026-05-30,0"}, delimiter = ',')
	void 월차대상자_검색(LocalDate now, int count) {
		Member member = memberFactory.defaultMember();
		VacationInfo info = vacationInfoRepository.save(new VacationInfo(15, 0, "testType", member.getId()));

		List<VacationInfo> result = vacationInfoRepository.findMonthlyVacationFrom(now.minusYears(1), now);

		assertThat(result).hasSize(count);
		if (count == 1) {
			assertThat(info).isEqualTo(result.getFirst());
		}
	}
}