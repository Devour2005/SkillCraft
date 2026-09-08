package com.skillcraft.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Read-only peek at management's `users` table (same Postgres instance,
 * `public` schema) - just enough to resolve an email address for events
 * that only carry a user id. Never written to from here.
 */
@Entity
@Table(name = "users", schema = "public")
public class UserView {

	@Id
	private Long id;

	@Column(name = "email")
	private String email;

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}
}
