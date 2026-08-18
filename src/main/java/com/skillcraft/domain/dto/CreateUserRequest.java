package com.skillcraft.domain.dto;

import com.develop_course.domain.User.UserRole;

public record CreateUserRequest(
		String email,
		String password,
		String firstName,
		String lastName,
		String phone,
		UserRole role
) {}