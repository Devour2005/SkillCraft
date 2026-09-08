package com.skillcraft.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A notification we would have sent. There is no real email/SMS provider
 * wired up yet - "sending" means logging and persisting a row here so the
 * effect of consuming a Kafka event is still observable.
 */
@Entity
@Table(name = "notifications", schema = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "recipient_user_id")
	private Long recipientUserId;

	@Column(name = "recipient_email", nullable = false)
	private String recipientEmail;

	@Column(name = "event_type", nullable = false, length = 50)
	private String eventType;

	@Column(nullable = false)
	private String subject;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "sent_at", nullable = false, updatable = false)
	@Builder.Default
	private Instant sentAt = Instant.now();
}
