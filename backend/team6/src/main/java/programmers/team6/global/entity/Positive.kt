package programmers.team6.global.entity;

public final class Positive {

	public static final int MIN_VALUE = 0;
	public static final String NEGATIVE_VALUE_ERROR_MESSAGE = "해당 값을 음수가 될 수 없습니다.";

	private final int value;

	public Positive(int value) {
		this.value = requirePositive(value);
	}

	private static int requirePositive(int value) {
		if (value < MIN_VALUE) {
			throw new IllegalArgumentException(NEGATIVE_VALUE_ERROR_MESSAGE);
		}
		return value;
	}

	public int toInt() {
		return value;
	}

	public boolean isEquals(Positive positive) {
		return this.value == positive.value;
	}

	public boolean isGraterThan(Positive totalCount) {
		return this.value > totalCount.value;
	}

	public boolean isLessThan(Positive totalCount) {
		return this.value < totalCount.value;
	}
}
