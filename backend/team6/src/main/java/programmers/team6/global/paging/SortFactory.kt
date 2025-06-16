package programmers.team6.global.paging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.NativeWebRequest;

public class SortFactory {

	public static final String SORT_PARAMETER_NAME = "sort";

	public Sort create(MergedAnnotation<PagingConfig> methodParameter, NativeWebRequest webRequest) {
		Sort.Direction defaultDirection = getDefaultDirection(methodParameter);
		Sort webSort = getWebSort(defaultDirection, webRequest);
		if (webSort.isSorted()) {
			return webSort;
		}
		return getConfigSort(methodParameter, defaultDirection);
	}

	private Sort.Direction getDefaultDirection(MergedAnnotation<PagingConfig> methodParameter) {
		return methodParameter.getEnum("direction", Sort.Direction.class);
	}

	private Sort getWebSort(Sort.Direction defaultDirection, NativeWebRequest webRequest) {
		String[] sorts = webRequest.getParameterValues(SORT_PARAMETER_NAME);
		if (ObjectUtils.isEmpty(sorts)) {
			return Sort.unsorted();
		}
		return Sort.by(toOrders(defaultDirection, sorts));
	}

	private List<Sort.Order> toOrders(Sort.Direction defaultDirection, String[] sorts) {
		List<Sort.Order> orders = new ArrayList<>();
		for (String sort : sorts) {
			orders.add(toOrder(sort, defaultDirection));
		}
		return orders;
	}

	private Sort.Order toOrder(String sort, Sort.Direction defaultDirection) {
		String[] values = sort.split(",", 2);
		if (ObjectUtils.isEmpty(values)) {
			throw new IllegalArgumentException("입력이 잘 못 되었습니다.");
		}
		if (values.length == 1) {
			return new Sort.Order(defaultDirection, values[0]);
		}
		return new Sort.Order(Sort.Direction.fromString(values[1]), values[0]);
	}

	private Sort getConfigSort(MergedAnnotation<PagingConfig> methodParameter, Sort.Direction defaultDirection) {
		String[] sorts = methodParameter.getStringArray(SORT_PARAMETER_NAME);
		if (ObjectUtils.isEmpty(sorts)) {
			return Sort.unsorted();
		}
		return Sort.by(defaultDirection, sorts);
	}
}
