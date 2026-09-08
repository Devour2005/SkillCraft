package com.skillcraft.service.impl;

import com.skillcraft.domain.Course;
import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import com.skillcraft.domain.dto.UpdateCourseRequest;
import com.skillcraft.domain.mapper.CourseMapper;
import com.skillcraft.repository.CourseRepository;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.CourseService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
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
				.orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

		if (teacher.getRole() != UserRole.TEACHER) {
			throw new IllegalArgumentException("User must have the TEACHER role");
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

	@Override
	@Transactional(readOnly = true)
	public List<CourseDto> getAllCourses() {
		return courseRepository.findAll()
				.stream()
				.map(courseMapper::toDto)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CourseDto getCourseById(Long id) {
		return courseRepository.findById(id)
				.map(courseMapper::toDto)
				.orElseThrow(() -> new EntityNotFoundException("Course not found"));
	}

	@Override
	@Transactional
	public CourseDto updateCourse(Long id, UpdateCourseRequest request) {
		Course course = courseRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Course not found"));

		User teacher = userRepository.findById(request.teacherId())
				.orElseThrow(() -> new EntityNotFoundException("Teacher not found"));

		if (teacher.getRole() != UserRole.TEACHER) {
			throw new IllegalArgumentException("User must have the TEACHER role");
		}

		course.setTitle(request.title());
		course.setDescription(request.description());
		course.setTeacher(teacher);
		course.setPrice(request.price());
		course.setIsArchived(request.isArchived());

		return courseMapper.toDto(course);
	}
}