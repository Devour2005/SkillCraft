package com.skillcraft.service;

import com.skillcraft.domain.dto.EnrollmentDto;
import java.util.List;

public interface EnrollmentService {

	EnrollmentDto enrollStudent(Long studentUserId, Long courseId);
	List<EnrollmentDto> getStudentEnrollments(Long studentUserId);
}
