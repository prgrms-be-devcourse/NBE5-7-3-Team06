package programmers.team6.domain.admin.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import programmers.team6.domain.admin.dto.response.DeptDropdownResponse;
import programmers.team6.domain.admin.entity.Dept;

public interface DeptRepository extends JpaRepository<Dept, Long> {

	@Query("""
		  SELECT new programmers.team6.domain.admin.dto.response.DeptDropdownResponse(d.id,d.deptName)
		  FROM Dept d
		""")
	List<DeptDropdownResponse> findAllDept();
	Optional<Dept> findByDeptName(String deptName);
}
