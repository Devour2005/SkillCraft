package com.skillcraft.notification.controller;

import com.skillcraft.notification.domain.Notification;
import com.skillcraft.notification.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug-only visibility into what this service would have sent. Not routed
 * through api-gateway yet and has no auth of its own - internal use only.
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationRepository notificationRepository;

	@GetMapping("/notifications")
	public List<Notification> getAll() {
		return notificationRepository.findAllByOrderBySentAtDesc();
	}

	@GetMapping("/notifications/user/{userId}")
	public List<Notification> getForUser(@PathVariable Long userId) {
		return notificationRepository.findAllByRecipientUserIdOrderBySentAtDesc(userId);
	}
}
