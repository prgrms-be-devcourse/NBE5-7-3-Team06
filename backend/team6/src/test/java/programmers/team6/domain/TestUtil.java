package programmers.team6.domain;

import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;

import java.time.LocalDateTime;

public class TestUtil {

    public static Member genMember(String name, Dept dept, Code position , LocalDateTime date, Role role) {

        return Member.builder()
                .name(name)
                .dept(dept)
                .position(position)
                .joinDate(date)
                .role(role)
                .build();
    }

    public static Dept genDept(Member member , String deptName ) {
        return   Dept.builder()
                .deptLeader(member)
                .deptName(deptName)
                .build();
    }

}
