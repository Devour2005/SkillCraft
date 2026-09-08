package com.skillcraft.gateway.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Public self-registration is intentionally limited to students. Staff
 * accounts (teacher, manager, accountant, admin) are created by an admin
 * through management's protected POST /api/users, reached via this gateway.
 */
public record RegisterRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8) String password,
		@NotBlank String firstName,
		@NotBlank String lastName
) {}
