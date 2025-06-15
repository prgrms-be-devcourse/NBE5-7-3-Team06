package programmers.team6.domain.member.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.support.PositionMother
import java.time.LocalDateTime

@DataJpaTest
@Import(MemberSearchRepository::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
internal class MemberSearchRepositoryTest @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val deptRepository: DeptRepository,
    private val codeRepository: CodeRepository,
    private val memberSearchRepository: MemberSearchRepository
) {


    @BeforeEach
    fun setUp() {
        val dept1 = deptRepository.save(Dept("name", null))
        val dept2 = deptRepository.save(Dept("name2", null))

        val employee = codeRepository.save(PositionMother.employee())


        memberRepository.saveAll(
            listOf(
                Member(
                    "test1",
                    dept1,
                    employee,
                    LocalDateTime.now(),
                    Role.USER
                ),
                Member(
                    "test2",
                    dept2,
                    employee,
                    LocalDateTime.now(),
                    Role.USER
                )
            )
        )
    }

    @Test
    fun `searchFrom should return members matching name and deptId`() {
        val dept = deptRepository.findAll().first()
        val pageable = PageRequest.of(0, 10)

        val result = memberSearchRepository.searchFrom("test", dept.id, pageable)

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].name).isEqualTo("test1")
    }

    @Test
    fun `searchFrom with ids should filter correctly`() {
        val allMembers = memberRepository.findAll()
        val dept = allMembers.first().dept
        val ids = listOf(allMembers[0].id) // Bob

        val result = memberSearchRepository.searchFrom(dept.id, null, ids, PageRequest.of(0, 10))

        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.content[0].name).isEqualTo("test1")
    }

}