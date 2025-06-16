package programmers.team6.global.paging

import org.springframework.data.domain.Sort

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PagingConfig(
    val size: Int = 10,
    val page: Int = 0,
    val maxSize: Int = 100,
    val sort: Array<String> = [],
    val direction: Sort.Direction = Sort.Direction.ASC
)
