package com.skillcraft.notification.service;

import com.skillcraft.notification.client.ManagementClient;
import com.skillcraft.notification.domain.Notification;
import com.skillcraft.notification.event.EnrollmentCreatedEvent;
import com.skillcraft.notification.event.PaymentProcessedEvent;
import com.skillcraft.notification.event.UserRegisteredEvent;
import com.skillcraft.notification.repository.NotificationRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * There is no real email/SMS provider wired up. "Sending" a notification
 * means logging it and persisting a row - real delivery is a future step.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationRepository notificationRepository;
	private final ManagementClient managementClient;

	public void notifyUserRegistered(UserRegisteredEvent event) {
		String subject = "Welcome to SkillCraft";
		String body = "Hi %s, your account (role: %s) has been created.".formatted(event.firstName(), event.role());
		send(event.userId(), event.email(), "USER_REGISTERED", subject, body);
	}

	public void notifyEnrollmentCreated(EnrollmentCreatedEvent event) {
		String subject = "You've been enrolled in " + event.courseTitle();
		String body = "Hi %s, you have been enrolled in \"%s\" (price: %s).".formatted(
				event.studentName(), event.courseTitle(), event.price());
		send(event.studentUserId(), resolveEmail(event.studentUserId()), "ENROLLMENT_CREATED", subject, body);
	}

	public void notifyPaymentProcessed(PaymentProcessedEvent event) {
		String subject = "Payment processed: " + event.type();
		String body = "A payment of %s (%s) was processed for your account. Comment: %s".formatted(
				event.amount(), event.type(), event.comment());
		send(event.targetUserId(), resolveEmail(event.targetUserId()), "PAYMENT_PROCESSED", subject, body);
	}

	private String resolveEmail(Long userId) {
		try {
			return managementClient.getUser(userId).email();
		} catch (FeignException ex) {
			log.warn("Could not resolve email for user {} from management: {}", userId, ex.getMessage());
			return null;
		}
	}

	private void send(Long recipientUserId, String recipientEmail, String eventType, String subject, String body) {
		String email = recipientEmail != null ? recipientEmail : "unknown-user-" + recipientUserId;
		log.info("[MOCK NOTIFICATION] to {} <{}> - {}: {}", recipientUserId, email, subject, body);

		notificationRepository.save(Notification.builder()
				.recipientUserId(recipientUserId)
				.recipientEmail(email)
				.eventType(eventType)
				.subject(subject)
				.body(body)
				.build());
	}
}
