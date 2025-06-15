package programmers.team6.domain.member.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import programmers.team6.domain.member.annotation.LoginMember
import programmers.team6.domain.member.dto.response.MemberLoginInfoResponse

@RestController
@RequestMapping("/members")
class MemberController{


    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    fun findLoginMemberInfo(@LoginMember memberInfo: MemberLoginInfoResponse): MemberLoginInfoResponse {
        return memberInfo
    }
}
