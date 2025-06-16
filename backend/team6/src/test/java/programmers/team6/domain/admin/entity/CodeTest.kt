package programmers.team6.domain.admin.entity

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.*


internal class CodeTest : StringSpec({
    "변경할 데이터가 유니크한 (groupCode, code)와 name일 경우, 성공적으로 code를 update" {
        // given
        val code = Code("origin_group_code", "origin_code", "origin_name")
        val updatedGroupCode = UUID.randomUUID().toString()
        val updatedCode = UUID.randomUUID().toString()
        val updatedName = UUID.randomUUID().toString()

        // when
        code.updateCode(updatedGroupCode, updatedCode, updatedName)

        // then
        code.groupCode shouldBe updatedGroupCode
        code.code shouldBe updatedCode
        code.name shouldBe updatedName
    }
})