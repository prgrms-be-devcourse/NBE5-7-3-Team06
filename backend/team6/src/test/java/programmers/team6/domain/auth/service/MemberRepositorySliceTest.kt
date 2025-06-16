package programmers.team6.domain.auth.service

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.entity.MemberInfo
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.support.PositionMother
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
internal class MemberRepositorySliceTest {
    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var codeRepository: CodeRepository

    @Autowired
    lateinit var deptRepository: DeptRepository

    @Test
    @DisplayName("이메일로 회원을 조회하면 존재하는 회원이 반환된다")
    fun returns_member_when_email_exists() {
        val dept = deptRepository.save(Dept(null, "개발팀", null))
        val position = codeRepository.save(PositionMother.employee())

        val member: Member = Member("test", dept, position, LocalDateTime.of(2024, 1, 1, 12, 0), Role.USER)
        val info = MemberInfo("birth", "test@gmail.com", "password")

        member.memberInfo = info

        memberRepository.save(member)

        val findMember = memberRepository.findByEmail(info.email)

        Assertions.assertThat(findMember).isNotNull
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional이 반환된다")
    fun returns_empty_optional_when_email_does_not_exist() {
        val dept = deptRepository.save(Dept(null, "개발팀", null))
        val position = codeRepository.save(PositionMother.employee())

        val member: Member = Member("test", dept, position, LocalDateTime.of(2024, 1, 1, 12, 0), Role.USER)
        val info = MemberInfo("birth", "test@gmail.com", "password")

        member.memberInfo = info
        memberRepository.save(member)

        val findMember = memberRepository.findByEmail("invalid@gmail.com")

        Assertions.assertThat(findMember).isNull()
    }
}
