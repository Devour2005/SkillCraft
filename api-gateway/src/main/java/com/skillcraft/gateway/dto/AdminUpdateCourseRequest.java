package com.skillcraft.gateway.dto;

import java.math.BigDecimal;

public record AdminUpdateCourseRequest(
		String title,
		String description,
		Long teacherId,
		BigDecimal price,
		Boolean isArchived
) {}
