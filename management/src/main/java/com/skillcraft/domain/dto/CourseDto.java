package com.skillcraft.domain.dto;

import java.math.BigDecimal;

public record CourseDto(
		Long id,
		String title,
		String description,
		Long teacherId,
		String teacherName,
		BigDecimal price,
		Boolean isArchived
) {}
