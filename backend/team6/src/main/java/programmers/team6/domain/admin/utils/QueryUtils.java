package programmers.team6.domain.admin.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.TypedQuery;
import lombok.experimental.UtilityClass;

/**
 * @author gunwoong
 */
@UtilityClass
public class QueryUtils {
	/**
	 * 제공된 쿼리를 페이징하여 페이지 객체 리턴
	 */
	public static <T> Page<T> makeQueryToPageable(TypedQuery<T> query, Pageable pageable, Long totalCount) {
		return new PageImpl<>(query.setFirstResult((int)pageable.getOffset())
			.setMaxResults(pageable.getPageSize())
			.getResultList(), pageable, totalCount);
	}
}
