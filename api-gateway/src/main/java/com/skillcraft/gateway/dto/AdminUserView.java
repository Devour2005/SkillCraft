package com.skillcraft.gateway.dto;

import com.skillcraft.gateway.domain.UserRole;

/**
 * Mirrors management's UserDto - the shape returned by /api/users.
 */
public record AdminUserView(
		Long id,
		String email,
		String firstName,
		String lastName,
		String phone,
		UserRole role,
		Boolean isActive,
		String createdAt
) {}
