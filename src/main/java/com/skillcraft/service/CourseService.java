package com.skillcraft.service;

import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseService {

	CourseDto createCourse(CreateCourseRequest request);
	Page<CourseDto> getActiveCourses(Pageable pageable);
}
