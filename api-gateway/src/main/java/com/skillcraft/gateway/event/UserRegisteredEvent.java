package com.skillcraft.gateway.event;

import java.time.Instant;

/**
 * Mirrors com.skillcraft.event.UserRegisteredEvent in management - field
 * names and JSON shape must match, consumers don't care which service sent it.
 */
public record UserRegisteredEvent(
		Long userId,
		String email,
		String firstName,
		String lastName,
		String role,
		Instant occurredAt
) {}
