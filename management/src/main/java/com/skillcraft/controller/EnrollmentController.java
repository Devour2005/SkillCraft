package com.skillcraft.controller;

import com.skillcraft.domain.dto.EnrollmentDto;
import com.skillcraft.service.EnrollmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<EnrollmentDto> enroll(@RequestParam Long studentUserId, @RequestParam Long courseId) {
		return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollStudent(studentUserId, courseId));
	}

	@GetMapping("/student/{studentUserId}")
	public List<EnrollmentDto> getStudentEnrollments(@PathVariable Long studentUserId) {
		return enrollmentService.getStudentEnrollments(studentUserId);
	}
}
