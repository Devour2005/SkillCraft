package com.skillcraft.gateway.dto;

import lombok.Data;

/**
 * Backing object for the HTML registration form (needs a no-arg constructor
 * + setters for classic Spring MVC data binding). RegisterRequest is the
 * equivalent used by the JSON /auth/register endpoint.
 */
@Data
public class RegisterForm {
	private String email;
	private String password;
	private String confirmPassword;
	private String firstName;
	private String lastName;
}
