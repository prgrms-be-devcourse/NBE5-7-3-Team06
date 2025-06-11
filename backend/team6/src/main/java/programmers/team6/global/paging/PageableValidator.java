package programmers.team6.global.paging;

import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PageableValidator {

	private final int maxPageSize;

	public void valid(Pageable pageable) {
		if (pageable.getPageSize() > maxPageSize) {
			throw new IllegalArgumentException("입력이 잘못 되었습니다.");
		}
	}
}
