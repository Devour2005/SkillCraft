package com.skillcraft.domain.dto;

import com.develop_course.domain.Enrollment.EnrollmentStatus;
import java.time.Instant;

public record EnrollmentDto(
		Long id,
		Long studentId,
		String studentName,
		Long courseId,
		String courseTitle,
		Instant enrolledAt,
		EnrollmentStatus status
) {}
