package com.skillcraft.billing.controller;

import com.skillcraft.billing.domain.Invoice;
import com.skillcraft.billing.repository.InvoiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Debug-only visibility into issued invoices. Not routed through
 * api-gateway yet and has no auth of its own - internal use only.
 */
@RestController
@RequiredArgsConstructor
public class InvoiceController {

	private final InvoiceRepository invoiceRepository;

	@GetMapping("/invoices")
	public List<Invoice> getAll() {
		return invoiceRepository.findAllByOrderByCreatedAtDesc();
	}

	@GetMapping("/invoices/student/{studentUserId}")
	public List<Invoice> getForStudent(@PathVariable Long studentUserId) {
		return invoiceRepository.findAllByStudentUserIdOrderByCreatedAtDesc(studentUserId);
	}
}
