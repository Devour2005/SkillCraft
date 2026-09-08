package com.skillcraft.controller;

import com.skillcraft.domain.dto.ProcessPaymentRequest;
import com.skillcraft.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;

	@PostMapping
	@PreAuthorize("hasRole('ACCOUNTANT')")
	public ResponseEntity<Void> processPayment(@Valid @RequestBody ProcessPaymentRequest request, Authentication authentication) {
		Long accountantId = (Long) authentication.getPrincipal();
		paymentService.processPayment(accountantId, request.targetUserId(), request.amount(), request.type(), request.comment());
		return ResponseEntity.status(201).build();
	}
}
