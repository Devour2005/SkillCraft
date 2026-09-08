package com.skillcraft.service;

import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import com.skillcraft.domain.dto.UpdateCourseRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

	CourseDto createCourse(CreateCourseRequest request);
	Page<CourseDto> getActiveCourses(Pageable pageable);
	List<CourseDto> getAllCourses();
	CourseDto getCourseById(Long id);
	CourseDto updateCourse(Long id, UpdateCourseRequest request);
}
