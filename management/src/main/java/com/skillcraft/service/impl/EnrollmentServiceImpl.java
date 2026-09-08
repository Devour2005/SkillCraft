package com.skillcraft.service.impl;

import com.skillcraft.domain.Course;
import com.skillcraft.domain.Enrollment;
import com.skillcraft.domain.Student;
import com.skillcraft.domain.dto.EnrollmentDto;
import com.skillcraft.domain.mapper.EnrollmentMapper;
import com.skillcraft.event.DomainEventPublisher;
import com.skillcraft.event.EnrollmentCreatedEvent;
import com.skillcraft.event.KafkaTopics;
import com.skillcraft.repository.CourseRepository;
import com.skillcraft.repository.EnrollmentRepository;
import com.skillcraft.repository.StudentRepository;
import com.skillcraft.service.EnrollmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final StudentRepository studentRepository;
	private final CourseRepository courseRepository;
	private final EnrollmentMapper enrollmentMapper;
	private final DomainEventPublisher eventPublisher;

	@Override
	@Transactional
	public EnrollmentDto enrollStudent(Long studentUserId, Long courseId) {
		Student student = studentRepository.findByUserId(studentUserId)
				.orElseThrow(() -> new EntityNotFoundException("Student not found"));

		if (enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
			throw new IllegalArgumentException("Student is already enrolled in this course");
		}

		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new EntityNotFoundException("Course not found"));

		Enrollment enrollment = Enrollment.builder()
				.student(student)
				.course(course)
				.status(Enrollment.EnrollmentStatus.ACTIVE)
				.build();

		Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

		String studentName = student.getUser().getFirstName() + " " + student.getUser().getLastName();
		eventPublisher.publish(KafkaTopics.ENROLLMENT_CREATED, String.valueOf(savedEnrollment.getId()),
				new EnrollmentCreatedEvent(savedEnrollment.getId(), studentUserId, studentName,
						course.getId(), course.getTitle(), course.getPrice(), Instant.now()));

		return enrollmentMapper.toDto(savedEnrollment);
	}

	@Override
	@Transactional(readOnly = true)
	public List<EnrollmentDto> getStudentEnrollments(Long studentUserId) {
		Student student = studentRepository.findByUserId(studentUserId)
				.orElseThrow(() -> new EntityNotFoundException("Student not found"));

		return enrollmentRepository.findAllByStudentId(student.getId())
				.stream()
				.map(enrollmentMapper::toDto)
				.toList();
	}
}
