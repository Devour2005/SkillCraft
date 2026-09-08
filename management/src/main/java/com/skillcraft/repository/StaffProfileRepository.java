package com.skillcraft.repository;

import com.skillcraft.domain.StaffProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

	@EntityGraph(attributePaths = {"user", "manager", "manager.user"})
	Optional<StaffProfile> findByUserId(Long userId);

	@EntityGraph(attributePaths = {"user"})
	List<StaffProfile> findAllByManagerId(Long managerId);
}
