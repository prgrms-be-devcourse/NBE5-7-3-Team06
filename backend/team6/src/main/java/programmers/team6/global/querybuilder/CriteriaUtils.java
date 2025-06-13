package programmers.team6.global.querybuilder;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.experimental.UtilityClass;

/**
 * @author gunwoong
 */
@UtilityClass
public class CriteriaUtils {
	/**
	 * 연관관계가 있는 클래스들을 Root에서 연달아 찾아가야할 경우 위와같이 Path를 찾아냄
	 * @param <X>
	 */
	public static <X> Path<String> searchPath(From<X, ?> root, SingularAttribute... mappedFields) {
		Path<String> path = root.get(mappedFields[0]);
		for (int i = 1; i < mappedFields.length; i++) {
			path = path.get(mappedFields[i]);
		}
		return path;
	}
}
