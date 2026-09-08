package com.skillcraft.billing.repository;

import com.skillcraft.billing.domain.Invoice;
import com.skillcraft.billing.domain.InvoiceStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

	List<Invoice> findAllByOrderByCreatedAtDesc();

	List<Invoice> findAllByStudentUserIdOrderByCreatedAtDesc(Long studentUserId);

	Optional<Invoice> findFirstByStudentUserIdAndStatusOrderByCreatedAtAsc(Long studentUserId, InvoiceStatus status);
}
