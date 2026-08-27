package com.skillcraft.repository;

import com.skillcraft.domain.Lesson;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

	@EntityGraph(attributePaths = {"course"})
	List<Lesson> findAllByCourseIdOrderByStartTimeAsc(Long courseId);

	@EntityGraph(attributePaths = {"course"})
	List<Lesson> findAllByStartTimeBetweenOrderByStartTimeAsc(Instant start, Instant end);
}