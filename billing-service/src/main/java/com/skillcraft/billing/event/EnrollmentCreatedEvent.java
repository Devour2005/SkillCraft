package com.skillcraft.billing.event;

import java.math.BigDecimal;
import java.time.Instant;

public record EnrollmentCreatedEvent(
		Long enrollmentId,
		Long studentUserId,
		String studentName,
		Long courseId,
		String courseTitle,
		BigDecimal price,
		Instant occurredAt
) {}
