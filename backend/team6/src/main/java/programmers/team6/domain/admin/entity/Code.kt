package programmers.team6.domain.admin.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.NoArgsConstructor
import programmers.team6.global.entity.BaseEntity

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "code", uniqueConstraints = [UniqueConstraint(columnNames = ["group_code", "code"])])
class Code(
    @field:Column(name = "group_code", nullable = false)
    var groupCode: String,
    @field:Column(nullable = false)
    var code: String,
    @field:Column(nullable = false)
    var name: String
) : BaseEntity() {
    @Id
    @Column(name = "code_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    fun updateCode(groupCode: String, code: String, name: String) {
        this.groupCode = groupCode
        this.code = code
        this.name = name
    }
}
