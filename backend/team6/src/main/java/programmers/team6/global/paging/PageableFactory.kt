package programmers.team6.global.paging;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.context.request.NativeWebRequest;

public class PageableFactory {

	private static final SortFactory DEFAULT_SORT_FACTORY = new SortFactory();
	private static final String PAGE_PARAM_NAME = "page";
	private static final String PAGE_SIZE_PARAM_NAME = "size";

	private final SortFactory sortFactory;

	public PageableFactory(SortFactory sortFactory) {
		this.sortFactory = sortFactory;
	}

	public PageableFactory() {
		this(DEFAULT_SORT_FACTORY);
	}

	public Pageable createPageable(MergedAnnotation<PagingConfig> config, NativeWebRequest webRequest) {
		int page = getPage(config, webRequest);
		int size = getSize(config, webRequest);
		Sort sort = sortFactory.create(config, webRequest);
		return PageRequest.of(page, size, sort);
	}

	private int getPage(MergedAnnotation<PagingConfig> methodParameter, NativeWebRequest webRequest) {
		String parameter = webRequest.getParameter(PAGE_PARAM_NAME);
		if (parameter != null) {
			return Integer.parseInt(parameter);
		}
		return methodParameter.getInt(PAGE_PARAM_NAME);
	}

	private int getSize(MergedAnnotation<PagingConfig> methodParameter, NativeWebRequest webRequest) {
		String parameter = webRequest.getParameter(PAGE_SIZE_PARAM_NAME);
		if (parameter != null) {
			return Integer.parseInt(parameter);
		}
		return methodParameter.getInt(PAGE_SIZE_PARAM_NAME);
	}
}
