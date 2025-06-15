package programmers.team6.domain.admin.support

import org.springframework.data.domain.Page
import programmers.team6.domain.member.entity.Member

class Members(private val members: Page<Member>) {
    fun toIds(): List<Long> {
        return members.stream().map { obj: Member -> obj.getId() }.toList()
    }

    fun toPages(): Page<Member> {
        return members
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Members

        return members == other.members
    }

    override fun hashCode(): Int {
        return members.hashCode()
    }
}
