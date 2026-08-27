package com.skillcraft.service;

import com.skillcraft.domain.Payment;
import java.math.BigDecimal;

public interface PaymentService {

	void processPayment(Long accountantId, Long targetUserId, BigDecimal amount, Payment.PaymentType type, String comment);
}
