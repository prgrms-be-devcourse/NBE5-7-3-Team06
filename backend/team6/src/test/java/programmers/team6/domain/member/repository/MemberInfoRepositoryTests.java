package programmers.team6.domain.member.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.member.enums.Role;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "programmers.team6.domain")
class MemberInfoRepositoryTests {

    @Autowired
    private MemberInfoRepository memberInfoRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DeptRepository deptRepository;

    @Autowired
    private CodeRepository codeRepository;



    @Test
    @DisplayName("이메일이 이미 존재하면 true를 반환한다")
    void existsByEmail_true() {

        String name = "member1";
        Long deptId = 1L;
        String email = "test@test.com";
        String password = "qwer1234!";
        String encodedPassword = "encoded1234";
        LocalDateTime joinDate =  LocalDateTime.of(2024, 1, 1, 12, 0);
        String birth = "1989-10-10";



        Dept dept = Dept.builder()
                .deptName("개발팀")
                .build();

        Code position = new Code("01", "POSITION", "사원");

        codeRepository.save(position);
        deptRepository.save(dept);

        MemberInfo memberInfo = MemberInfo.builder()
                .email(email)
                .password(encodedPassword)
                .birth(birth)
                .build();

        Member member = Member.builder()
                .name(name)
                .dept(dept)
                .position(position)
                .role(Role.USER)
                .joinDate(joinDate)
                .build();

        member.setMemberInfo(memberInfo);
        memberRepository.save(member);

        boolean exists = memberInfoRepository.existsByEmail("sehee@test.com");

        assertThat(exists).isTrue();


    }


}