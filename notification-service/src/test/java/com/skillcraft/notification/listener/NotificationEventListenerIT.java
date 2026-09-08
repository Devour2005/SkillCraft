package com.skillcraft.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.notification.TestcontainersConfiguration;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies the consumer side against a real broker and a real database: a
 * plain JSON message on each topic ends up as a Notification row.
 *
 * The test DB only carries this service's own `notification` schema (from
 * its own Flyway migration) - `public.users` is management's table, so it's
 * faked here with the one column (email) this service actually reads.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class NotificationEventListenerIT {

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaContainer kafkaContainer;

	private KafkaProducer<String, String> producer;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS public.users (id BIGINT PRIMARY KEY, email VARCHAR(255))");

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
		jdbcTemplate.update("INSERT INTO public.users (id, email) VALUES (?, ?)", userId, "enrolled-student@example.com");

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
		jdbcTemplate.update("INSERT INTO public.users (id, email) VALUES (?, ?)", userId, "payer@example.com");

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
