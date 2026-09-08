package com.skillcraft.gateway.dto;

import java.math.BigDecimal;

/**
 * Mirrors management's CourseDto - the shape returned by /api/courses/all.
 */
public record AdminCourseView(
		Long id,
		String title,
		String description,
		Long teacherId,
		String teacherName,
		BigDecimal price,
		Boolean isArchived
) {}
