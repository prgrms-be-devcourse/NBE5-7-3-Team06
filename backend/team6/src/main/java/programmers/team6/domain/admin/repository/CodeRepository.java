package programmers.team6.domain.admin.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import programmers.team6.domain.admin.dto.CodeInfo;
import programmers.team6.domain.admin.dto.response.CodeDropdownResponse;
import programmers.team6.domain.admin.dto.response.CodeReadResponse;
import programmers.team6.domain.admin.entity.Code;

public interface CodeRepository extends JpaRepository<Code, Long> {
	Optional<Code> findByIdAndGroupCode(Long id, String groupCode);

	Optional<Code> findByGroupCodeAndCode(String groupCode, String code);

	@Query("""
		  SELECT new programmers.team6.domain.admin.dto.response.CodeDropdownResponse (c.code,c.name)
		  FROM Code c
		  WHERE c.groupCode = :groupCode
		""")
	List<CodeDropdownResponse> findByGroupCode(@Param("groupCode") String groupCode);

	@Query("""
			SELECT new programmers.team6.domain.admin.dto.response.CodeReadResponse(c.id,c.groupCode,c.code,c.name)
			FROM Code c
			WHERE (:groupCode IS NULL OR c.groupCode = :groupCode)
		""")
	Page<CodeReadResponse> findCodePage(Pageable pageable, @Param("groupCode") String groupCode);

	@Query(value = "select new programmers.team6.domain.admin.dto.CodeInfo(c.id,c.name) from Code c where c.groupCode = :groupCode")
	List<CodeInfo> findCodeInfosByGroupCode(@Param("groupCode") String groupCode);

	@Query(value = "select c.groupCode from Code c group by c.groupCode")
	List<String> findGroupCodes();

	boolean existsByGroupCodeAndCode(String groupCode, String code);
}
