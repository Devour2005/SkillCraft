package com.skillcraft.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCourseRequest(
		@NotBlank String title,
		String description,
		@NotNull Long teacherId,
		@NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
		@NotNull Boolean isArchived
) {}
