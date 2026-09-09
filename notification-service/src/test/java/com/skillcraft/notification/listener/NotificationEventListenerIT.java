package com.skillcraft.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.notification.TestcontainersConfiguration;
import com.skillcraft.notification.client.ManagementClient;
import com.skillcraft.notification.client.ManagementUserDto;
import com.skillcraft.notification.domain.Notification;
import com.skillcraft.notification.event.EnrollmentCreatedEvent;
import com.skillcraft.notification.event.KafkaTopics;
import com.skillcraft.notification.event.PaymentProcessedEvent;
import com.skillcraft.notification.event.UserRegisteredEvent;
import com.skillcraft.notification.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Verifies the consumer side against a real broker and a real database: a
 * plain JSON message on each topic ends up as a Notification row.
 *
 * management (the actual owner of the `users` table this service used to
 * read directly) isn't part of this test's Testcontainers setup, so the
 * Feign client that now resolves emails is mocked instead.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-not-used-outside-the-test-suite-0123456789")
class NotificationEventListenerIT {

	@Autowired
	private NotificationRepository notificationRepository;

	@MockitoBean
	private ManagementClient managementClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaContainer kafkaContainer;

	private KafkaProducer<String, String> producer;

	@BeforeEach
	void setUp() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producer = new KafkaProducer<>(props);
	}

	@AfterEach
	void tearDown() {
		producer.close();
	}

	@Test
	void userRegisteredEvent_isPersistedAsNotification() throws Exception {
		long userId = System.nanoTime();
		UserRegisteredEvent event = new UserRegisteredEvent(userId, "new-student@example.com", "Dana", "White", "STUDENT", Instant.now());

		publish(KafkaTopics.USER_REGISTERED, event);

		Notification notification = awaitNotification(userId, "USER_REGISTERED");
		assertEquals("new-student@example.com", notification.getRecipientEmail());
		assertTrue(notification.getBody().contains("Dana"));
	}

	@Test
	void enrollmentCreatedEvent_resolvesEmailFromUsersTableAndIsPersisted() throws Exception {
		long userId = System.nanoTime();
		when(managementClient.getUser(userId)).thenReturn(new ManagementUserDto(userId, "enrolled-student@example.com"));

		EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(1L, userId, "Dana White", 10L, "Algebra 101",
				new java.math.BigDecimal("150.00"), Instant.now());

		publish(KafkaTopics.ENROLLMENT_CREATED, event);

		Notification notification = awaitNotification(userId, "ENROLLMENT_CREATED");
		assertEquals("enrolled-student@example.com", notification.getRecipientEmail());
		assertTrue(notification.getBody().contains("Algebra 101"));
	}

	@Test
	void paymentProcessedEvent_resolvesEmailFromUsersTableAndIsPersisted() throws Exception {
		long userId = System.nanoTime();
		when(managementClient.getUser(userId)).thenReturn(new ManagementUserDto(userId, "payer@example.com"));

		PaymentProcessedEvent event = new PaymentProcessedEvent(1L, 2L, userId, new java.math.BigDecimal("200.00"),
				"STUDENT_TUITION", "Tuition", Instant.now());

		publish(KafkaTopics.PAYMENT_PROCESSED, event);

		Notification notification = awaitNotification(userId, "PAYMENT_PROCESSED");
		assertEquals("payer@example.com", notification.getRecipientEmail());
		assertTrue(notification.getBody().contains("200.00"));
	}

	private void publish(String topic, Object event) throws Exception {
		producer.send(new ProducerRecord<>(topic, String.valueOf(System.nanoTime()), objectMapper.writeValueAsString(event))).get();
	}

	private Notification awaitNotification(long recipientUserId, String eventType) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (System.currentTimeMillis() < deadline) {
			List<Notification> matches = notificationRepository.findAllByRecipientUserIdOrderBySentAtDesc(recipientUserId)
					.stream()
					.filter(n -> n.getEventType().equals(eventType))
					.toList();
			Optional<Notification> found = matches.stream().findFirst();
			if (found.isPresent()) {
				return found.get();
			}
			Thread.sleep(300);
		}
		fail("No notification of type " + eventType + " persisted for user " + recipientUserId + " within timeout");
		throw new AssertionError("unreachable");
	}
}
