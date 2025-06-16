package programmers.team6.domain.admin.entity

import jakarta.persistence.*
import programmers.team6.domain.member.entity.Member
import programmers.team6.global.entity.BaseEntity
import programmers.team6.global.exception.code.NotFoundErrorCode
import programmers.team6.global.exception.customException.NotFoundException

@Entity
class Dept(
    @Id
    @Column(name = "dept_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var deptName: String,

    @OneToOne
    @JoinColumn(name = "dept_leader_id")
    var deptLeader: Member? = null

) : BaseEntity() {

    fun appointLeader(leader: Member) {
        this.deptLeader = leader
    }

    fun deptLeaderOrThrow(): Member {
        return this.deptLeader ?: throw NotFoundException(NotFoundErrorCode.NOT_FOUND_DEPT_LEADER)
    }

    val isHrDept: Boolean
        get() = this.deptName == "인사팀"
}
