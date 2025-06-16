package programmers.team6.domain.member.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import programmers.team6.domain.member.entity.Member
import programmers.team6.domain.member.repository.MemberRepository
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

@Service
class MemberService( private val memberRepository: MemberRepository) {

    fun findById(memberId: Long): Member {
        return memberRepository.findByIdOrNull(memberId)
            ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_MEMBER)
    }
}
