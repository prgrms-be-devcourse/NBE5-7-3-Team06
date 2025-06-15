package programmers.team6.domain.admin.service

import org.springframework.stereotype.Service
import programmers.team6.domain.admin.dto.response.DeptDropdownResponse
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

@Service
class DeptService(private val deptRepository: DeptRepository) {


    fun findAllDept(): List<DeptDropdownResponse> {
        return deptRepository.findAllDept()
    }

    fun findByDeptName(deptName: String):Dept {
        return deptRepository.findByDeptName(deptName) ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT)
    }
}
