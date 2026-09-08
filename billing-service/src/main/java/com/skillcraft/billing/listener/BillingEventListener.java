package com.skillcraft.billing.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.billing.event.EnrollmentCreatedEvent;
import com.skillcraft.billing.event.KafkaTopics;
import com.skillcraft.billing.event.PaymentProcessedEvent;
import com.skillcraft.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingEventListener {

	private final BillingService billingService;
	private final ObjectMapper objectMapper;

	@KafkaListener(topics = KafkaTopics.ENROLLMENT_CREATED)
	public void onEnrollmentCreated(String payload) {
		try {
			billingService.issueInvoice(objectMapper.readValue(payload, EnrollmentCreatedEvent.class));
		} catch (Exception ex) {
			log.error("Failed to process {} event: {}", KafkaTopics.ENROLLMENT_CREATED, payload, ex);
		}
	}

	@KafkaListener(topics = KafkaTopics.PAYMENT_PROCESSED)
	public void onPaymentProcessed(String payload) {
		try {
			billingService.reconcilePayment(objectMapper.readValue(payload, PaymentProcessedEvent.class));
		} catch (Exception ex) {
			log.error("Failed to process {} event: {}", KafkaTopics.PAYMENT_PROCESSED, payload, ex);
		}
	}
}
