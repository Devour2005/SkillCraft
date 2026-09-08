package com.skillcraft.domain.dto;

import com.skillcraft.domain.Payment.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProcessPaymentRequest(
		@NotNull Long targetUserId,
		@NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
		@NotNull PaymentType type,
		String comment
) {}
