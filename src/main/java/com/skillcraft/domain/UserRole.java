package com.skillcraft.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	ADMIN(1, "ADMIN"),
	STUDENT(2, "STUDENT"),
	TEACHER(3, "TEACHER"),
	MANAGER(4, "MANAGER"),
	ACCOUNTANT(5, "ACCOUNTANT");

	private final Integer value;
	private final String name;

	public static UserRole fromValue(String value) {
		for (UserRole entity : UserRole.values()) {
			if (entity.getName().equals(value)) {
				return entity;
			}
		}
		throw new IllegalArgumentException("Unknown type: " + value);
	}
}
