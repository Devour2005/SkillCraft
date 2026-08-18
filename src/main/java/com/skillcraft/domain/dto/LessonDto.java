package com.skillcraft.domain.dto;

import java.time.Instant;

public record LessonDto(
		Long id,
		Long courseId,
		String courseTitle,
		String title,
		Instant startTime,
		Instant endTime
) {}