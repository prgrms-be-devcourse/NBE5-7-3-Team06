package programmers.team6.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import programmers.team6.domain.member.entity.MemberInfo;

public interface MemberInfoRepository extends JpaRepository<MemberInfo, Long> {

	public boolean existsByEmail(String email);
}
