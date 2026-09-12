package com.poc.payment.repository;

import com.poc.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantIdAndApprovalNo(String merchantId, String approvalNo);

    /** 멱등성 판단용 조회 — 같은 거래고유번호의 기존 승인을 찾는다. */
    Optional<Payment> findByMerchantIdAndTxId(String merchantId, String txId);
}
