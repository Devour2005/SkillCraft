package com.skillcraft.service.impl;

import com.skillcraft.domain.Course;
import com.skillcraft.domain.Enrollment;
import com.skillcraft.domain.Student;
import com.skillcraft.domain.dto.EnrollmentDto;
import com.skillcraft.domain.mapper.EnrollmentMapper;
import com.skillcraft.repository.CourseRepository;
import com.skillcraft.repository.EnrollmentRepository;
import com.skillcraft.repository.StudentRepository;
import com.skillcraft.service.EnrollmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final EnrollmentMapper enrollmentMapper;

	@Override
	@Transactional
	public EnrollmentDto enrollStudent(Long studentId, Long courseId) {
		if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
			throw new IllegalArgumentException("Студент уже записан на этот курс");
		}

		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new EntityNotFoundException("Студент не найден"));

		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new EntityNotFoundException("Курс не найден"));

		Enrollment enrollment = Enrollment.builder()
				.student(student)
				.course(course)
				.status(Enrollment.EnrollmentStatus.ACTIVE)
				.build();

		Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
		return enrollmentMapper.toDto(savedEnrollment);
	}

	@Transactional(readOnly = true)
	public List<EnrollmentDto> getStudentEnrollments(Long studentId) {
		return enrollmentRepository.findAllByStudentId(studentId)
				.stream()
				.map(enrollmentMapper::toDto)
				.toList();
	}
}