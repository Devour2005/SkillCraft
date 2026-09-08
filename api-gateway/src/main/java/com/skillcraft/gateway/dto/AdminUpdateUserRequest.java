package com.skillcraft.gateway.dto;

public record AdminUpdateUserRequest(
		String firstName,
		String lastName,
		String phone,
		Boolean isActive
) {}
