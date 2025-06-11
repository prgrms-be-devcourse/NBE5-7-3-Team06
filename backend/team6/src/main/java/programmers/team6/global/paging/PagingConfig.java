package programmers.team6.global.paging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.domain.Sort;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PagingConfig {

	int size() default 10;

	int page() default 0;

	int maxSize() default 100;

	String[] sort() default {};

	Sort.Direction direction() default Sort.Direction.ASC;
}
