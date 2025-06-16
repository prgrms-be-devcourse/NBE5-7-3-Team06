package programmers.team6.domain.vacation.repository.factory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.admin.repository.CodeRepository
import programmers.team6.domain.admin.repository.DeptRepository
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.enums.Role
import programmers.team6.domain.member.repository.MemberRepository
import java.time.LocalDateTime

@Component
class TestMemberFactory {

    @Autowired
    private lateinit var codeRepository: CodeRepository

    @Autowired
    private lateinit var deptRepository: DeptRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    fun defaultMember(): Member {
        val joinDate = LocalDateTime.of(2025, 10, 31, 0, 0)
        val code = getOrCreate()
        val dept = deptRepository.save(Dept(deptName = "code", deptLeader = null))
        val member = Member("test1", dept, code, joinDate, Role.USER)
        return memberRepository.save(member)
    }

    private fun getOrCreate(): Code {
        val all = codeRepository.findAll()
        return if (all.isEmpty()) {
            codeRepository.save(Code("1", "1", "code"))
        } else {
            all.first()
        }
    }
}