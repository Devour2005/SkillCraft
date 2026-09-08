package com.skillcraft.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.notification.event.EnrollmentCreatedEvent;
import com.skillcraft.notification.event.KafkaTopics;
import com.skillcraft.notification.event.PaymentProcessedEvent;
import com.skillcraft.notification.event.UserRegisteredEvent;
import com.skillcraft.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Events are consumed as plain JSON strings (not Spring Kafka's type-header
 * based JSON deserialization) so producers and this consumer never need to
 * share a Java package - see DomainEventPublisher in management/api-gateway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

	private final NotificationService notificationService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = KafkaTopics.USER_REGISTERED)
	public void onUserRegistered(String payload) {
		try {
			notificationService.notifyUserRegistered(objectMapper.readValue(payload, UserRegisteredEvent.class));
		} catch (Exception ex) {
			log.error("Failed to process {} event: {}", KafkaTopics.USER_REGISTERED, payload, ex);
		}
	}

	@KafkaListener(topics = KafkaTopics.ENROLLMENT_CREATED)
	public void onEnrollmentCreated(String payload) {
		try {
			notificationService.notifyEnrollmentCreated(objectMapper.readValue(payload, EnrollmentCreatedEvent.class));
		} catch (Exception ex) {
			log.error("Failed to process {} event: {}", KafkaTopics.ENROLLMENT_CREATED, payload, ex);
		}
	}

	@KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED)
	public void onPaymentProcessed(String payload) {
		try {
			notificationService.notifyPaymentProcessed(objectMapper.readValue(payload, PaymentProcessedEvent.class));
		} catch (Exception ex) {
			log.error("Failed to process {} event: {}", KafkaTopics.PAYMENT_PROCESSED, payload, ex);
		}
	}
}
