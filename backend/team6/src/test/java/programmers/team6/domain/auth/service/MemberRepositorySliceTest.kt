package programmers.team6.domain.auth.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import jakarta.transaction.Transactional;
import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.admin.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.support.PositionMother;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class MemberRepositorySliceTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private CodeRepository codeRepository;

	@Autowired
	private DeptRepository deptRepository;

	@Test
	@DisplayName("이메일로 회원을 조회하면 존재하는 회원이 반환된다")
	void returns_member_when_email_exists() {
		Dept dept = deptRepository.save(new Dept("test", null));
		Code position = codeRepository.save(PositionMother.employee());

		Member member = Member.builder()
			.role(Role.USER)
			.dept(dept)
			.joinDate(LocalDateTime.now())
			.name("test")
			.position(position)
			.build();
		MemberInfo info = new MemberInfo("birth", "test@gmail.com", "password");
		member.setMemberInfo(info);
		memberRepository.save(member);

		Optional<Member> memberOptional = memberRepository.findByEmail(info.getEmail());

		assertThat(memberOptional).isPresent();
	}

	@Test
	@DisplayName("존재하지 않는 이메일로 조회하면 빈 Optional이 반환된다")
	void returns_empty_optional_when_email_does_not_exist() {
		Dept dept = deptRepository.save(new Dept("test", null));
		Code position = codeRepository.save(PositionMother.employee());

		Member member = Member.builder()
			.role(Role.USER)
			.dept(dept)
			.joinDate(LocalDateTime.now())
			.name("test")
			.position(position)
			.build();
		MemberInfo info = new MemberInfo("birth", "test@gmail.com", "password");
		member.setMemberInfo(info);
		memberRepository.save(member);

		Optional<Member> memberOptional = memberRepository.findByEmail("invalid@gmail.com");

		assertThat(memberOptional).isEmpty();
	}
}
