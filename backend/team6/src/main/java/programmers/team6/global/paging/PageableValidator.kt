package programmers.team6.global.paging

import org.springframework.data.domain.Pageable

class PageableValidator(private val maxPageSize:Int = 0) {

    fun valid(pageable: Pageable) {
        require(pageable.pageSize <= maxPageSize) { "입력이 잘못 되었습니다." }
    }
}
