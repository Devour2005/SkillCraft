package com.skillcraft.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessedEvent(
		Long paymentId,
		Long accountantId,
		Long targetUserId,
		BigDecimal amount,
		String type,
		String comment,
		Instant occurredAt
) {}
