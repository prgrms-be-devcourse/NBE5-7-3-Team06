package programmers.team6.domain.admin.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import programmers.team6.domain.admin.dto.CodeInfo
import programmers.team6.domain.admin.dto.response.CodeDropdownResponse
import programmers.team6.domain.admin.dto.response.CodeReadResponse
import programmers.team6.domain.admin.entity.Code

interface CodeRepository : JpaRepository<Code, Long> {
    fun findByIdAndGroupCode(id: Long, groupCode: String): Code?

    fun findByGroupCodeAndCode(groupCode: String?, code: String?): Code?

    @Query(
        """
		  SELECT new programmers.team6.domain.admin.dto.response.CodeDropdownResponse (c.code,c.name)
		  FROM Code c
		  WHERE c.groupCode = :groupCode
		"""
    )
    fun findByGroupCode(@Param("groupCode") groupCode: String): MutableList<CodeDropdownResponse>

    @Query(
        """
			SELECT new programmers.team6.domain.admin.dto.response.CodeReadResponse(c.id,c.groupCode,c.code,c.name)
			FROM Code c
			WHERE (:groupCode IS NULL OR c.groupCode = :groupCode)
		"""
    )
    fun findCodePage(pageable: Pageable, @Param("groupCode") groupCode: String?): Page<CodeReadResponse>

        @Query(value = "select new programmers.team6.domain.admin.dto.CodeInfo(c.id,c.name) from Code c where c.groupCode = :groupCode")
        fun findCodeInfosByGroupCode(@Param("groupCode") groupCode: String?): List<CodeInfo>

    @Query(value = "select c.groupCode from Code c group by c.groupCode")
    fun findGroupCodes(): List<String>

    fun existsByGroupCodeAndCode(groupCode: String, code: String): Boolean

    fun findCodeById(id: Long): Code?
}
