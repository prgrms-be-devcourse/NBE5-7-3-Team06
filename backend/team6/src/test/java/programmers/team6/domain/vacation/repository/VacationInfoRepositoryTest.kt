package programmers.team6.domain.vacation.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import programmers.team6.domain.vacation.entity.VacationInfo
import programmers.team6.domain.vacation.repository.factory.TestMemberFactory
import java.time.LocalDate

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Import(value = [TestMemberFactory::class])
@ActiveProfiles("test")
internal class VacationInfoRepositoryTest @Autowired constructor(

    private val vacationInfoRepository: VacationInfoRepository,

    private val memberFactory: TestMemberFactory
) {

    @ParameterizedTest
    @CsvSource(value = ["2026-10-31,1", "2026-10-30,0"], delimiter = ',')
    fun 연차대상자_검색(now: LocalDate, count: Int) {
        val member = memberFactory.defaultMember()
        val info = vacationInfoRepository.save(
            VacationInfo(
                15.0, 0.0, "testType",
                member.id!!
            )
        )

        val result = vacationInfoRepository.findAnnualVacationFrom(now.minusYears(1), now)

        assertThat(result).hasSize(count)
        if (count == 1) {
            assertThat<VacationInfo>(info).isEqualTo(result.first())
        }
    }

    @ParameterizedTest
    @CsvSource(value = ["2026-02-28,1", "2026-05-31,1", "2026-05-30,0"], delimiter = ',')
    fun 월차대상자_검색(now: LocalDate, count: Int) {
        val member = memberFactory!!.defaultMember()
        val info = vacationInfoRepository!!.save(
            VacationInfo(
                15.0, 0.0, "testType",
                member.id!!
            )
        )

        val result = vacationInfoRepository.findMonthlyVacationFrom(now.minusYears(1), now)

        assertThat(result).hasSize(count)
        if (count == 1) {
            assertThat<VacationInfo>(info).isEqualTo(result.first())
        }
    }
}