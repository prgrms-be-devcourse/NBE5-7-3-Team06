package programmers.team6.domain.member.entity

import jakarta.persistence.*
import programmers.team6.domain.admin.entity.Code
import programmers.team6.domain.admin.entity.Dept
import programmers.team6.domain.member.enums.Role
import programmers.team6.global.exception.code.BadRequestErrorCode
import programmers.team6.global.exception.customException.BadRequestException
import java.time.LocalDate


@Entity
@Table(name = "members")
class Member(
    name: String,
    dept: Dept,
    position: Code,
    joinDate: LocalDate,
    role: Role,
){

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long? = null

    @Column(nullable = false)
    val name: String = name

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    val dept:Dept = dept

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    val position:Code= position

    @Column(nullable = false)
    val joinDate: LocalDate = LocalDate.now()

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role:Role = role

    @JoinColumn(name = "member_info_id")
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var memberInfo : MemberInfo? = null

    fun approve(){
        if(role != Role.PENDING){
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
        }
        role = Role.USER
    }

    fun  validateDeletableOnReject(){
        if(this.role != Role.PENDING){
            throw BadRequestException(BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE)
        }
    }

    fun isHrMember():Boolean{
        return dept.isHrDept()
    }

}

