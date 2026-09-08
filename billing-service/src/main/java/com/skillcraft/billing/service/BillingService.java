package com.skillcraft.billing.service;

import com.skillcraft.billing.domain.Invoice;
import com.skillcraft.billing.domain.InvoiceStatus;
import com.skillcraft.billing.event.EnrollmentCreatedEvent;
import com.skillcraft.billing.event.PaymentProcessedEvent;
import com.skillcraft.billing.repository.InvoiceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PDF generation (real invoices, real receipts) is deferred. For now this
 * service only tracks invoice state and logs what a document would have
 * contained - that's the "mock" stand-in requested in place of PDFs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

	private static final String STUDENT_TUITION = "STUDENT_TUITION";

	private final InvoiceRepository invoiceRepository;

	@Transactional
	public void issueInvoice(EnrollmentCreatedEvent event) {
		Invoice invoice = Invoice.builder()
				.enrollmentId(event.enrollmentId())
				.studentUserId(event.studentUserId())
				.courseId(event.courseId())
				.courseTitle(event.courseTitle())
				.amount(event.price())
				.status(InvoiceStatus.PENDING)
				.build();

		Invoice saved = invoiceRepository.save(invoice);
		log.info("[MOCK INVOICE #{}] {} owes {} for \"{}\" - PDF generation not implemented yet",
				saved.getId(), event.studentName(), event.price(), event.courseTitle());
	}

	@Transactional
	public void reconcilePayment(PaymentProcessedEvent event) {
		if (!STUDENT_TUITION.equals(event.type())) {
			log.info("Payment {} is of type {}, not tuition - nothing to reconcile", event.paymentId(), event.type());
			return;
		}

		Optional<Invoice> pending = invoiceRepository.findFirstByStudentUserIdAndStatusOrderByCreatedAtAsc(
				event.targetUserId(), InvoiceStatus.PENDING);

		if (pending.isEmpty()) {
			log.warn("Tuition payment {} received for user {} but no pending invoice was found", event.paymentId(), event.targetUserId());
			return;
		}

		Invoice invoice = pending.get();
		invoice.setStatus(InvoiceStatus.PAID);
		invoice.setPaidAt(event.occurredAt());
		invoice.setRelatedPaymentId(event.paymentId());

		log.info("[MOCK RECEIPT] Invoice #{} for \"{}\" marked PAID by payment #{} - PDF generation not implemented yet",
				invoice.getId(), invoice.getCourseTitle(), event.paymentId());
	}
}
