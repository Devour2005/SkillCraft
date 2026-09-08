package com.skillcraft.gateway.domain;

/**
 * Mirrors com.skillcraft.domain.UserRole in the management module.
 * Duplicated on purpose: the two services share the `users` table today but
 * are meant to be independently deployable, so they don't share Java code.
 */
public enum UserRole {
	ADMIN, STUDENT, TEACHER, MANAGER, ACCOUNTANT
}
