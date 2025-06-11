package programmers.team6.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import programmers.team6.domain.auth.dto.request.MemberSignUpRequest;
import programmers.team6.domain.auth.service.AuthService;
import programmers.team6.domain.member.entity.Code;
import programmers.team6.domain.member.entity.Dept;
import programmers.team6.domain.member.entity.Member;
import programmers.team6.domain.member.entity.MemberInfo;
import programmers.team6.domain.member.enums.BasicCodeInfo;
import programmers.team6.domain.member.enums.Role;
import programmers.team6.domain.member.repository.CodeRepository;
import programmers.team6.domain.member.repository.DeptRepository;
import programmers.team6.domain.member.repository.MemberRepository;
import programmers.team6.domain.member.util.mapper.CodeMapper;
import programmers.team6.domain.vacation.entity.ApprovalStep;
import programmers.team6.domain.vacation.entity.VacationRequest;
import programmers.team6.domain.vacation.enums.ApprovalStatus;
import programmers.team6.domain.vacation.enums.VacationRequestStatus;
import programmers.team6.domain.vacation.repository.ApprovalStepRepository;
import programmers.team6.domain.vacation.repository.VacationRequestRepository;

@Configuration
@RequiredArgsConstructor
@Transactional
public class CodeInitializationConfig {
	private final CodeRepository codeRepository;
	private final DeptRepository deptRepository;
	private final MemberRepository memberRepository;
	private final AuthService authService;
	private final PasswordEncoder passwordEncoder;
	private final VacationRequestRepository vacationRequestRepository;
	private final ApprovalStepRepository approvalStepRepository;

	/**
	 * 현재 개발환경이고 다른 엔티티의 변수 수정 가능성이 있는 상황에서 우선적으로 CommandLineRunner를 활용하여 개발하였음, 그럼으로 yml의 profile은 dev로 작성 필요
	 * 만약, 운영환경으로 넘어간다면 flyway로 수정하여 해당 부분또한 insert문이 포함된 sql 실행하도록 바꾸면 좋을듯
	 * @return
	 * @author gunwoong
	 */
	@Bean
	@Profile("dev")
	public CommandLineRunner initData() {
		return args -> {
			if (codeRepository.count() == 0) {  // 데이터베이스에 데이터가 없으면 삽입
				insert("POSITION", "01", "사원");
				insert("POSITION", "02", "대리");
				insert("POSITION", "03", "과장");
				insert("POSITION", "04", "부장");

				for (int i = 0; i < BasicCodeInfo.values().length; i++) {
					codeRepository.save(CodeMapper.toCode(BasicCodeInfo.values()[i]));
				}

				DeptInsert("인사팀");
				DeptInsert("개발팀");
				DeptInsert("영업팀");

				insertAdmin(new MemberSignUpRequest("관리자", "admin@a.com", 1L, "04", LocalDateTime.of(2023, 5, 15, 0, 0),
					"000000",
					"123456q!"));
				insertMember();
				setDeptLeader();

				Member member = memberRepository.findById(2L).get();
				Member member2 = memberRepository.findById(3L).get();
				Member member3 = memberRepository.findById(4L).get();
				Code vacationType = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", "01").get();
				Code vacationType1 = codeRepository.findByGroupCodeAndCode("VACATION_TYPE", "05").get();

				VacationRequest vacationRequest = new VacationRequest(member, LocalDateTime.now(),
					LocalDateTime.of(2025, 5, 22, 0, 0),
					"사유",
					vacationType, VacationRequestStatus.APPROVED, 1);

				VacationRequest vacationRequest2 = new VacationRequest(member2, LocalDateTime.of(2025, 5, 18, 9, 0),
					LocalDateTime.of(2025, 5, 20, 13, 0),
					"사유",
					vacationType1, VacationRequestStatus.APPROVED, 1);

				VacationRequest vacationRequest3 = new VacationRequest(member3, LocalDateTime.of(2025, 5, 30, 0, 0),
					LocalDateTime.of(2025, 5, 30, 0, 0),
					"사유",
					vacationType, VacationRequestStatus.APPROVED, 1);

				vacationRequest = insertVacation(vacationRequest);
				vacationRequest2 = insertVacation(vacationRequest2);
				vacationRequest3 = insertVacation(vacationRequest3);

				approvalStepRepository.save(new ApprovalStep(0, ApprovalStatus.PENDING, member2, vacationRequest));
				approvalStepRepository.save(new ApprovalStep(1, ApprovalStatus.PENDING, member3, vacationRequest));
				approvalStepRepository.save(new ApprovalStep(0, ApprovalStatus.PENDING, member, vacationRequest2));
				approvalStepRepository.save(new ApprovalStep(1, ApprovalStatus.PENDING, member3, vacationRequest2));
				approvalStepRepository.save(new ApprovalStep(0, ApprovalStatus.PENDING, member, vacationRequest3));
				approvalStepRepository.save(new ApprovalStep(1, ApprovalStatus.PENDING, member2, vacationRequest3));
			}

		};
	}

	private void insert(String groupCode, String code, String name) {
		codeRepository.save(Code.builder()
			.groupCode(groupCode)
			.code(code)
			.name(name)
			.build());
	}

	private void DeptInsert(String deptName) {
		deptRepository.save(Dept.builder().deptName(deptName).build());
	}

	private void insertAdmin(MemberSignUpRequest request) {
		Dept dept = deptRepository.findByDeptName("인사팀").orElseThrow();
		Code code = codeRepository.findByGroupCodeAndCode("POSITION", "04").orElseThrow();

		String encodedPassword = passwordEncoder.encode(request.password());

		MemberInfo memberInfo = MemberInfo.builder()
			.birth(request.birth())
			.email(request.email())
			.password(encodedPassword)
			.build();

		Member member = Member.builder()
			.name(request.name())
			.dept(dept)
			.position(code)
			.joinDate(request.joinDate())
			.role(Role.ADMIN)
			.build();

		member.setMemberInfo(memberInfo);

		memberRepository.save(member);

	}

	private void insertMember() {
		authService.signUp(
			new MemberSignUpRequest("김부장", "l1@a.com", 1L, "04", LocalDateTime.of(2023, 5, 15, 0, 0), "850101",
				"123456q!"));
		authService.signUp(
			new MemberSignUpRequest("이부장", "l2@a.com", 2L, "04", LocalDateTime.of(2023, 5, 15, 0, 0), "831111",
				"123456q!"));
		authService.signUp(
			new MemberSignUpRequest("박부장", "l3@a.com", 3L, "04", LocalDateTime.of(2023, 5, 15, 0, 0), "871101",
				"123456q!"));
	}

	private void setDeptLeader() {
		Member m1 = memberRepository.findById(2L).orElseThrow();
		Member m2 = memberRepository.findById(3L).orElseThrow();
		Member m3 = memberRepository.findById(4L).orElseThrow();

		Dept d1 = deptRepository.findById(1L).orElseThrow();
		Dept d2 = deptRepository.findById(2L).orElseThrow();
		Dept d3 = deptRepository.findById(3L).orElseThrow();

		d1.appointLeader(m1);
		d2.appointLeader(m2);
		d3.appointLeader(m3);

		deptRepository.saveAll(List.of(d1, d2, d3));

	}

	private VacationRequest insertVacation(VacationRequest request) {
		return vacationRequestRepository.save(request);
	}

}
