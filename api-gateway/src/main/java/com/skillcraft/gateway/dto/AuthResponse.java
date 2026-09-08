package com.skillcraft.gateway.dto;

import com.skillcraft.gateway.domain.UserRole;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresInMs,
		Long userId,
		String email,
		UserRole role
) {}
