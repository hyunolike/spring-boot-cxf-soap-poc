package com.poc.payment.repository;

import com.poc.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantIdAndApprovalNo(String merchantId, String approvalNo);
}
