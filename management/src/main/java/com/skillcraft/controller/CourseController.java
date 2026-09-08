package com.skillcraft.controller;

import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import com.skillcraft.domain.dto.UpdateCourseRequest;
import com.skillcraft.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

	private final CourseService courseService;

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CreateCourseRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
	}

	@GetMapping
	public Page<CourseDto> getActiveCourses(Pageable pageable) {
		return courseService.getActiveCourses(pageable);
	}

	@GetMapping("/all")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public List<CourseDto> getAllCourses() {
		return courseService.getAllCourses();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public CourseDto getCourse(@PathVariable Long id) {
		return courseService.getCourseById(id);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	public CourseDto updateCourse(@PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
		return courseService.updateCourse(id, request);
	}
}
