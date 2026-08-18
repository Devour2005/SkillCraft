package com.skillcraft.domain.dto;

import com.skillcraft.domain.User.UserRole;
import java.time.Instant;

public record UserDto(
		Long id,
		String email,
		String firstName,
		String lastName,
		String phone,
		UserRole role,
		Boolean isActive,
		Instant createdAt
) {}
