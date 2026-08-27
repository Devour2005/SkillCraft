package com.skillcraft.service.impl;

import com.skillcraft.domain.Payment;
import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.repository.PaymentRepository;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final UserRepository userRepository;
	private final PaymentRepository paymentRepository;

	@Override
	@Transactional
	public void processPayment(Long accountantId, Long targetUserId, BigDecimal amount, Payment.PaymentType type, String comment) {
		User accountant = userRepository.findById(accountantId)
				.orElseThrow(() -> new EntityNotFoundException("Бухгалтер не найден"));

		if (accountant.getRole() != UserRole.ACCOUNTANT) {
			throw new SecurityException("Только бухгалтер может проводить платежи");
		}

		User targetUser = userRepository.findById(targetUserId)
				.orElseThrow(() -> new EntityNotFoundException("Целевой пользователь не найден"));

		Payment payment = Payment.builder()
				.accountant(accountant)
				.targetUser(targetUser)
				.amount(amount)
				.type(type)
				.status(Payment.PaymentStatus.COMPLETED)
				.comment(comment)
				.build();

		paymentRepository.save(payment);
	}
}