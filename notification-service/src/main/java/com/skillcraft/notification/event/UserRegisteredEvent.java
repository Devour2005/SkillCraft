package com.skillcraft.notification.event;

import java.time.Instant;

public record UserRegisteredEvent(
		Long userId,
		String email,
		String firstName,
		String lastName,
		String role,
		Instant occurredAt
) {}
