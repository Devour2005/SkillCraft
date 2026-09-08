package com.skillcraft.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.TestcontainersConfiguration;
import com.skillcraft.domain.Payment;
import com.skillcraft.domain.UserRole;
import com.skillcraft.domain.dto.CourseDto;
import com.skillcraft.domain.dto.CreateCourseRequest;
import com.skillcraft.domain.dto.CreateUserRequest;
import com.skillcraft.domain.dto.UserDto;
import com.skillcraft.service.CourseService;
import com.skillcraft.service.EnrollmentService;
import com.skillcraft.service.PaymentService;
import com.skillcraft.service.UserService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End-to-end proof that each write that should announce itself on Kafka
 * actually does, against a real broker (Testcontainers), not a mock.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-not-used-outside-the-test-suite-0123456789")
class DomainEventPublishingIT {

	@Autowired
	private UserService userService;

	@Autowired
	private CourseService courseService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaContainer kafkaContainer;

	private KafkaConsumer<String, String> consumer;

	@BeforeEach
	void setUpConsumer() {
		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "domain-event-it-" + System.nanoTime());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumer = new KafkaConsumer<>(props);
	}

	@AfterEach
	void tearDownConsumer() {
		consumer.close();
	}

	@Test
	void createUser_publishesUserRegisteredEvent() throws Exception {
		consumer.subscribe(Collections.singletonList(KafkaTopics.USER_REGISTERED));
		primeAssignment();

		String email = "kafka-user-" + System.nanoTime() + "@example.com";
		UserDto created = userService.createUser(
				new CreateUserRequest(email, "password123", "Kaf", "Ka", null, UserRole.STUDENT));

		UserRegisteredEvent event = awaitEvent(KafkaTopics.USER_REGISTERED, UserRegisteredEvent.class);

		assertEquals(created.id(), event.userId());
		assertEquals(email, event.email());
		assertEquals("STUDENT", event.role());
	}

	@Test
	void enrollStudent_publishesEnrollmentCreatedEvent() throws Exception {
		UserDto teacher = userService.createUser(new CreateUserRequest(
				"kafka-teacher-" + System.nanoTime() + "@example.com", "password123", "Tom", "Reed", null, UserRole.TEACHER));
		UserDto student = userService.createUser(new CreateUserRequest(
				"kafka-student-" + System.nanoTime() + "@example.com", "password123", "Dana", "White", null, UserRole.STUDENT));
		CourseDto course = courseService.createCourse(
				new CreateCourseRequest("Kafka Basics", "Intro", teacher.id(), new BigDecimal("199.00")));

		consumer.subscribe(Collections.singletonList(KafkaTopics.ENROLLMENT_CREATED));
		primeAssignment();

		enrollmentService.enrollStudent(student.id(), course.id());

		EnrollmentCreatedEvent event = awaitEvent(KafkaTopics.ENROLLMENT_CREATED, EnrollmentCreatedEvent.class);

		assertEquals(student.id(), event.studentUserId());
		assertEquals(course.id(), event.courseId());
		assertEquals(course.title(), event.courseTitle());
		assertEquals(0, new BigDecimal("199.00").compareTo(event.price()));
	}

	@Test
	void processPayment_publishesPaymentProcessedEvent() throws Exception {
		UserDto accountant = userService.createUser(new CreateUserRequest(
				"kafka-accountant-" + System.nanoTime() + "@example.com", "password123", "Olga", "Popova", null, UserRole.ACCOUNTANT));
		UserDto target = userService.createUser(new CreateUserRequest(
				"kafka-target-" + System.nanoTime() + "@example.com", "password123", "Dana", "White", null, UserRole.STUDENT));

		consumer.subscribe(Collections.singletonList(KafkaTopics.PAYMENT_PROCESSED));
		primeAssignment();

		paymentService.processPayment(accountant.id(), target.id(), new BigDecimal("150.00"), Payment.PaymentType.STUDENT_TUITION, "Test tuition");

		PaymentProcessedEvent event = awaitEvent(KafkaTopics.PAYMENT_PROCESSED, PaymentProcessedEvent.class);

		assertEquals(accountant.id(), event.accountantId());
		assertEquals(target.id(), event.targetUserId());
		assertEquals("STUDENT_TUITION", event.type());
		assertEquals(0, new BigDecimal("150.00").compareTo(event.amount()));
	}

	private void primeAssignment() {
		consumer.poll(Duration.ofMillis(200));
	}

	private <T> T awaitEvent(String topic, Class<T> type) throws Exception {
		long deadline = System.currentTimeMillis() + 20_000;
		while (System.currentTimeMillis() < deadline) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
			for (ConsumerRecord<String, String> record : records) {
				if (record.topic().equals(topic)) {
					return objectMapper.readValue(record.value(), type);
				}
			}
		}
		fail("No event received on topic " + topic + " within timeout");
		throw new AssertionError("unreachable");
	}
}
