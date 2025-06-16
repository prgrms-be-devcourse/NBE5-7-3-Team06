package programmers.team6.domain.member.entity

import jakarta.persistence.*
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.entity.BaseEntity
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import java.time.LocalDateTime


@Entity
@Table(name = "members")
open class Member(

    @field:Column(nullable = false)
    val name: String,

    @field:JoinColumn(name = "dept_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val dept: Dept?,

    @field:JoinColumn(name = "position_id")
    @field:ManyToOne(fetch = FetchType.LAZY)
    val position: Code,

    @field:Column(nullable = false)
    val joinDate: LocalDateTime,

    role: Role,

    ) : BaseEntity() {
    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @JoinColumn(name = "member_info_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var memberInfo: MemberInfo? = null

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, name = "role")
    private var _role: Role = role

    val role: Role
        get() = _role


    fun approve() {
        if (_role != Role.PENDING) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
        }
        _role = Role.USER
    }

    fun validateDeletableOnReject() {
        if (_role != Role.PENDING) {
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
        }
    }

    val isHrMember: Boolean
        get() = this.dept != null && dept!!.isHrDept

}