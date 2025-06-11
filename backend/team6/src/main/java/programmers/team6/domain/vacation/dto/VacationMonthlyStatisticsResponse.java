package programmers.team6.domain.vacation.dto;

public record VacationMonthlyStatisticsResponse(
	Long memberId,
	String userName,
	double totalCount,
	double usedCount,
	double remainCount,
	double january,
	double february,
	double march,
	double april,
	double may,
	double june,
	double july,
	double august,
	double september,
	double october,
	double november,
	double december
) {
}
