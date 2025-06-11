package programmers.team6.domain.member.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.global.exception.code.BadRequestErrorCode;
import programmers.team6.global.exception.customException.BadRequestException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static programmers.team6.domain.TestUtil.genDept;
import static programmers.team6.domain.TestUtil.genMember;

class MemberTests {

@Test
@DisplayName("멤버 엔티티 권한을 승인으로 업데이트한다.")
void member_approve_success()  {

    Member member = genMember("member1",null,null, LocalDateTime.of(2024, 1, 1, 12, 0),Role.PENDING);

    member.approve();

    assertThat(member.getRole()).isEqualTo(Role.USER);
}



@Test
@DisplayName("멤버 엔티티 권한 업데이트 시 대기중이 아니면 실패 ")
void member_approve_fail() {

    Member member = genMember("member1",null,null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    assertThatThrownBy(
            () -> {
                member.approve();
            }
    ).isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("errorCode", BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE);

}

@Test
@DisplayName("멤버 엔티티의 권한이 대기중이 아니면 예외를 반환한다")
void validation_deletable_on_reject() {

    Member member = genMember("member1",null,null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    assertThatThrownBy(
            () -> {
                member.validateDeletableOnReject();
            }
    ).isInstanceOf(BadRequestException.class)
            .hasFieldOrPropertyWithValue("errorCode", BadRequestErrorCode.BAD_REQUEST_MEMBER_ROLE);
}

@Test
@DisplayName("멤버 엔티티의 부서정보가 비어있지 않고 부서가 인사팀이면 true를 반환한다.")
void is_hr_member_true()  {

    Member member = genMember("member1",null,null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    Member hrMember = Member.builder()
            .name("member2")
            .joinDate(LocalDateTime.now())
            .role(Role.USER)
            .dept(genDept(member,"인사팀"))
            .build();

    assertThat(hrMember.isHrMember()).isTrue();

}

@Test
@DisplayName("멤버 엔티티의 부서가 인사팀이 아니면 false를  반환한다.")
void is_hr_member_not_hrTeam_false()  {

    Member member = genMember("member1",null,null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    Member hrMember = genMember("member2",genDept(member,"개발1팀"),null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    assertThat(hrMember.isHrMember()).isFalse();

}



@Test
@DisplayName("멤버 엔티티의 부서가 null이면 false를  반환한다.")
void is_hr_member_dept_null_false()  {

    Member member = genMember("member1",null,null,LocalDateTime.of(2024, 1, 1, 12, 0),Role.USER);

    assertThat(member.isHrMember()).isFalse();

}

}