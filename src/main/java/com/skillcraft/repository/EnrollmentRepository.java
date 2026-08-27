package com.skillcraft.repository;

import com.skillcraft.domain.Enrollment;
import com.skillcraft.domain.Enrollment.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

	Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

	@EntityGraph(attributePaths = {"student", "student.user", "course"})
	List<Enrollment> findAllByStudentId(Long studentId);

	@EntityGraph(attributePaths = {"student", "student.user", "course"})
	List<Enrollment> findAllByCourseIdAndStatus(Long courseId, EnrollmentStatus status);
}