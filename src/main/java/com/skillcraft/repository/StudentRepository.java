package com.skillcraft.repository;

import com.skillcraft.domain.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	@EntityGraph(attributePaths = {"user"})
	Optional<Student> findByUserId(Long userId);
}