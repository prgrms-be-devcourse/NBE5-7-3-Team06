package programmers.team6.domain.member.entity

import jakarta.persistence.*
import programmers.team6.global.entity.BaseEntity

@Entity
class MemberInfo (

    @field:Column(nullable = false)
     val birth: String,

    @field:Column(nullable = false)
    val email: String,

    @field:Column(nullable = false)
    val password: String
) : BaseEntity() {
    @Id
    @Column(name = "member_info_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null


}