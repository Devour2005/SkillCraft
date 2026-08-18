package com.skillcraft.domain.dto;

import java.math.BigDecimal;

public record CreateCourseRequest(
		String title,
		String description,
		Long teacherId,
		BigDecimal price
) {}
