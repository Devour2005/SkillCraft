package com.skillcraft.repository;

import com.skillcraft.domain.Manager;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

	@EntityGraph(attributePaths = {"user"})
	Optional<Manager> findByUserId(Long userId);
}