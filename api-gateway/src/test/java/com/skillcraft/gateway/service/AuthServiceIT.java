package com.skillcraft.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.gateway.TestcontainersConfiguration;
import com.skillcraft.gateway.dto.AuthResponse;
import com.skillcraft.gateway.dto.LoginRequest;
import com.skillcraft.gateway.dto.RegisterRequest;
import com.skillcraft.gateway.event.KafkaTopics;
import com.skillcraft.gateway.event.UserRegisteredEvent;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Registration is the one write api-gateway does directly against the
 * shared `users`/`students` tables (everything else goes through
 * management), so this both proves that write is correct and that it
 * announces itself on Kafka - against a real Postgres and a real broker.
 *
 * The test DB starts empty (this module runs no migrations of its own -
 * management owns the schema), so the handful of columns actually touched
 * are created by hand below, native `user_role` enum included.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = "jwt.secret=test-only-secret-not-used-outside-the-test-suite-0123456789")
class AuthServiceIT {

	@Autowired
	private AuthService authService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaContainer kafkaContainer;

	private KafkaConsumer<String, String> consumer;

	@BeforeEach
	void setUp() {
		jdbcTemplate.execute("""
				DO $$ BEGIN
					CREATE TYPE user_role AS ENUM ('MANAGER','ADMIN','TEACHER','STUDENT','ACCOUNTANT');
				EXCEPTION WHEN duplicate_object THEN NULL; END $$;
				""");
		jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS users (
					id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
					email VARCHAR(255) NOT NULL UNIQUE,
					password_hash VARCHAR(255) NOT NULL,
					first_name VARCHAR(100) NOT NULL,
					last_name VARCHAR(100) NOT NULL,
					phone VARCHAR(30),
					role user_role NOT NULL,
					is_active BOOLEAN NOT NULL DEFAULT true,
					created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
				)
				""");
		jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS students (
					id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
					user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
					birth_date DATE,
					notes TEXT
				)
				""");

		Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "auth-service-it-" + System.nanoTime());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumer = new KafkaConsumer<>(props);
	}

	@AfterEach
	void tearDown() {
		consumer.close();
	}

	@Test
	void register_createsUserAndStudentRow_andPublishesUserRegisteredEvent() throws Exception {
		consumer.subscribe(Collections.singletonList(KafkaTopics.USER_REGISTERED));
		consumer.poll(Duration.ofMillis(200));

		String email = "gateway-it-" + System.nanoTime() + "@example.com";
		AuthResponse response = authService.register(new RegisterRequest(email, "password123", "Dana", "White"));

		assertEquals(email, response.email());
		assertEquals("Bearer", response.tokenType());

		Long studentRowCount = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM students WHERE user_id = ?", Long.class, response.userId());
		assertEquals(1L, studentRowCount);

		UserRegisteredEvent event = awaitEvent();
		assertEquals(response.userId(), event.userId());
		assertEquals(email, event.email());
		assertEquals("STUDENT", event.role());
	}

	@Test
	void login_rejectsWrongPassword() {
		String email = "gateway-it-login-" + System.nanoTime() + "@example.com";
		authService.register(new RegisterRequest(email, "password123", "Dana", "White"));

		assertThrows(BadCredentialsException.class,
				() -> authService.login(new LoginRequest(email, "wrong-password")));
	}

	private UserRegisteredEvent awaitEvent() throws Exception {
		long deadline = System.currentTimeMillis() + 20_000;
		while (System.currentTimeMillis() < deadline) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
			for (ConsumerRecord<String, String> record : records) {
				if (record.topic().equals(KafkaTopics.USER_REGISTERED)) {
					return objectMapper.readValue(record.value(), UserRegisteredEvent.class);
				}
			}
		}
		fail("No event received on topic " + KafkaTopics.USER_REGISTERED + " within timeout");
		throw new AssertionError("unreachable");
	}
}
