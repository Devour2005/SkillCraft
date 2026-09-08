package com.skillcraft.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
		@NotBlank String firstName,
		@NotBlank String lastName,
		String phone,
		@NotNull Boolean isActive
) {}
