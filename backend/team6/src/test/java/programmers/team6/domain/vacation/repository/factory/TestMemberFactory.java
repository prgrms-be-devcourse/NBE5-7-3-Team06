package programmers.team6.domain.vacation.repository.factory;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import programmers.team6.domain.admin.entity.Code;
import programmers.team6.domain.admin.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.admin.repository.CodeRepository;
import programmers.team6.domain.admin.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberRepository;

@Component
public class TestMemberFactory {

	@Autowired
	private CodeRepository codeRepository;
	@Autowired
	private DeptRepository deptRepository;
	@Autowired
	private MemberRepository memberRepository;

	public Member defaultMember() {
		LocalDateTime joinDate = LocalDateTime.of(2025, 10, 31, 0, 0);
		Code code = getOrCreate();
		Dept dept = deptRepository.save(new Dept("code", null));
		Member member = new Member("test1", dept, code, joinDate, Role.USER);
		return memberRepository.save(member);
	}

	private Code getOrCreate() {
		List<Code> all = codeRepository.findAll();
		if (all.isEmpty())
			return codeRepository.save(new Code("1", "1", "code"));
		return all.getFirst();
	}
}
