package programmers.team6.domain.member.repository

import org.springframework.data.jpa.repository.JpaRepository
import programmers.team6.domain.member.entity.MemberInfo

interface MemberInfoRepository : JpaRepository<MemberInfo, Long> {
    fun existsByEmail(email: String): Boolean
}
