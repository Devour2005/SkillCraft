package com.skillcraft.repository;

import com.skillcraft.domain.Payment;
import com.skillcraft.domain.Payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	@EntityGraph(attributePaths = {"accountant", "targetUser"})
	Page<Payment> findAllByTargetUserId(Long targetUserId, Pageable pageable);

	@EntityGraph(attributePaths = {"accountant", "targetUser"})
	List<Payment> findAllByStatus(PaymentStatus status);
}