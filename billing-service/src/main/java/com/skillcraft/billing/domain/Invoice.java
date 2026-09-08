package com.skillcraft.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A billable charge raised when a student enrolls in a course. PDF
 * generation is deferred - for now, "issuing" and "receipting" an invoice
 * just means writing this row and logging what would have been produced.
 */
@Entity
@Table(name = "invoices", schema = "billing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "enrollment_id")
	private Long enrollmentId;

	@Column(name = "student_user_id", nullable = false)
	private Long studentUserId;

	@Column(name = "course_id")
	private Long courseId;

	@Column(name = "course_title", nullable = false, length = 200)
	private String courseTitle;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	@Builder.Default
	private InvoiceStatus status = InvoiceStatus.PENDING;

	@Column(name = "related_payment_id")
	private Long relatedPaymentId;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Builder.Default
	private Instant createdAt = Instant.now();

	@Column(name = "paid_at")
	private Instant paidAt;
}
