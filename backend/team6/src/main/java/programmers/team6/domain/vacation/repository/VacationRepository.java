package programmers.team6.domain.vacation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import programmers.team6.domain.vacation.entity.VacationInfo;

public interface VacationRepository extends JpaRepository<VacationInfo, Integer> {

	Optional<VacationInfo> findByMemberIdAndVacationType(Long memberId, String vacationType);
}