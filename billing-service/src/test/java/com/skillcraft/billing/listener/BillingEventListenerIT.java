package com.skillcraft.billing.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillcraft.billing.TestcontainersConfiguration;
import com.skillcraft.billing.domain.Invoice;
import com.skillcraft.billing.domain.InvoiceStatus;
import com.skillcraft.billing.event.EnrollmentCreatedEvent;
import com.skillcraft.billing.event.KafkaTopics;
import com.skillcraft.billing.event.PaymentProcessedEvent;
import com.skillcraft.billing.repository.InvoiceRepository;
import java.math.BigDecimal;
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
import org.testcontainers.kafka.KafkaContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies the consumer side against a real broker and a real database: an
 * enrollment.created message issues a PENDING invoice, and a matching
 * payment.processed message closes it.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BillingEventListenerIT {

	@Autowired
	private InvoiceRepository invoiceRepository;

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
	void enrollmentCreated_issuesPendingInvoice() throws Exception {
		long studentUserId = System.nanoTime();
		EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(1L, studentUserId, "Dana White", 10L, "Algebra 101",
				new BigDecimal("150.00"), Instant.now());

		publish(KafkaTopics.ENROLLMENT_CREATED, event);

		Invoice invoice = awaitInvoice(studentUserId);
		assertEquals(InvoiceStatus.PENDING, invoice.getStatus());
		assertEquals("Algebra 101", invoice.getCourseTitle());
		assertEquals(0, new BigDecimal("150.00").compareTo(invoice.getAmount()));
	}

	@Test
	void paymentProcessed_closesMatchingPendingInvoice() throws Exception {
		long studentUserId = System.nanoTime();
		EnrollmentCreatedEvent enrollmentEvent = new EnrollmentCreatedEvent(2L, studentUserId, "Dana White", 11L, "Geometry 201",
				new BigDecimal("175.00"), Instant.now());
		publish(KafkaTopics.ENROLLMENT_CREATED, enrollmentEvent);
		Invoice pending = awaitInvoice(studentUserId);
		assertEquals(InvoiceStatus.PENDING, pending.getStatus());

		PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(99L, 1L, studentUserId, new BigDecimal("175.00"),
				"STUDENT_TUITION", "Tuition", Instant.now());
		publish(KafkaTopics.PAYMENT_PROCESSED, paymentEvent);

		Invoice paid = awaitPaidInvoice(studentUserId);
		assertEquals(InvoiceStatus.PAID, paid.getStatus());
		assertEquals(99L, paid.getRelatedPaymentId());
	}

	private void publish(String topic, Object event) throws Exception {
		producer.send(new ProducerRecord<>(topic, String.valueOf(System.nanoTime()), objectMapper.writeValueAsString(event))).get();
	}

	private Invoice awaitInvoice(long studentUserId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (System.currentTimeMillis() < deadline) {
			List<Invoice> invoices = invoiceRepository.findAllByStudentUserIdOrderByCreatedAtDesc(studentUserId);
			if (!invoices.isEmpty()) {
				return invoices.get(0);
			}
			Thread.sleep(300);
		}
		fail("No invoice created for student " + studentUserId + " within timeout");
		throw new AssertionError("unreachable");
	}

	private Invoice awaitPaidInvoice(long studentUserId) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (System.currentTimeMillis() < deadline) {
			Optional<Invoice> paid = invoiceRepository.findAllByStudentUserIdOrderByCreatedAtDesc(studentUserId)
					.stream()
					.filter(i -> i.getStatus() == InvoiceStatus.PAID)
					.findFirst();
			if (paid.isPresent()) {
				return paid.get();
			}
			Thread.sleep(300);
		}
		fail("Invoice for student " + studentUserId + " was never marked PAID within timeout");
		throw new AssertionError("unreachable");
	}
}
