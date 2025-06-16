package programmers.team6.domain.admin.controller

import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import programmers.team6.domain.admin.dto.response.DeptDropdownResponse
import programmers.team6.domain.admin.service.DeptService

@RestController
@RequestMapping("/depts")
class DeptQueryController(
    private val deptService: DeptService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun allDept(): List<DeptDropdownResponse> {
        return deptService.findAllDept()
    }
    
}
