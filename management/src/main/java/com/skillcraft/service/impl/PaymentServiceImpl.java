package com.skillcraft.service.impl;

import com.skillcraft.domain.Payment;
import com.skillcraft.domain.User;
import com.skillcraft.domain.UserRole;
import com.skillcraft.event.DomainEventPublisher;
import com.skillcraft.event.KafkaTopics;
import com.skillcraft.event.PaymentProcessedEvent;
import com.skillcraft.repository.PaymentRepository;
import com.skillcraft.repository.UserRepository;
import com.skillcraft.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final UserRepository userRepository;
	private final PaymentRepository paymentRepository;
	private final DomainEventPublisher eventPublisher;

	@Override
	@Transactional
	public void processPayment(Long accountantId, Long targetUserId, BigDecimal amount, Payment.PaymentType type, String comment) {
		User accountant = userRepository.findById(accountantId)
				.orElseThrow(() -> new EntityNotFoundException("Accountant not found"));

		if (accountant.getRole() != UserRole.ACCOUNTANT) {
			throw new SecurityException("Only an accountant can process payments");
		}

		User targetUser = userRepository.findById(targetUserId)
				.orElseThrow(() -> new EntityNotFoundException("Target user not found"));

		Payment payment = Payment.builder()
				.accountant(accountant)
				.targetUser(targetUser)
				.amount(amount)
				.type(type)
				.status(Payment.PaymentStatus.COMPLETED)
				.comment(comment)
				.build();

		Payment savedPayment = paymentRepository.save(payment);

		eventPublisher.publish(KafkaTopics.PAYMENT_PROCESSED, String.valueOf(savedPayment.getId()),
				new PaymentProcessedEvent(savedPayment.getId(), accountantId, targetUserId, amount,
						type.name(), comment, Instant.now()));
	}
}