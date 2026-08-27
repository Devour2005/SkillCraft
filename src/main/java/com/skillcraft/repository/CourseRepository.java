package com.skillcraft.repository;

import com.skillcraft.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

	@EntityGraph(attributePaths = {"teacher"})
	Page<Course> findAllByIsArchivedFalse(Pageable pageable);

	@EntityGraph(attributePaths = {"teacher"})
	List<Course> findAllByTeacherId(Long teacherId);
}
