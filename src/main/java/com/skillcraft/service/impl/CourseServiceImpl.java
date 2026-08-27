package com.skillcraft.service.impl;

import com.skillcraft.domain.Course;
import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import com.skillcraft.domain.mapper.CourseMapper;
import com.skillcraft.repository.CourseRepository;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

	private final CourseRepository courseRepository;
	private final UserRepository userRepository;
	private final CourseMapper courseMapper;

	@Override
	@Transactional
	public CourseDto createCourse(CreateCourseRequest request) {
		User teacher = userRepository.findById(request.teacherId())
				.orElseThrow(() -> new EntityNotFoundException("Преподаватель не найден"));

		if (teacher.getRole() != UserRole.TEACHER) {
			throw new IllegalArgumentException("Пользователь должен иметь роль TEACHER");
		}

		Course course = courseMapper.toEntity(request);
		course.setTeacher(teacher);

		Course savedCourse = courseRepository.save(course);
		return courseMapper.toDto(savedCourse);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CourseDto> getActiveCourses(Pageable pageable) {
		return courseRepository.findAllByIsArchivedFalse(pageable)
				.map(courseMapper::toDto);
	}
}