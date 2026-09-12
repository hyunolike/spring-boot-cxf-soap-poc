package com.poc.payment.service;

import com.poc.payment.domain.Payment;
import com.poc.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 실제 비즈니스 로직 담당 (SOAP를 전혀 모르는 계층).
 * SOAP를 REST로 바꿔도 이 클래스는 그대로 재사용 가능해야 한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;

    /**
     * 멱등 승인. 같은 (merchantId, txId) 요청이 이미 승인돼 있으면
     * 새로 승인하지 않고 기존 승인을 그대로 돌려준다.
     */
    @Transactional
    public ApprovalResult approve(String merchantId, String txId,
                                  String cardNo, BigDecimal amount) {
        validate(merchantId, txId, cardNo, amount);

        Optional<Payment> already = paymentRepository.findByMerchantIdAndTxId(merchantId, txId);
        if (already.isPresent()) {
            return ApprovalResult.duplicated(already.get());
        }

        Payment payment = Payment.approve(
                merchantId,
                txId,
                mask(cardNo),
                amount,
                generateApprovalNo());
        // 사전 조회와 insert 사이의 경쟁은 uk_payments_merchant_tx가 막는다.
        // saveAndFlush로 제약 위반을 이 자리에서 바로 터뜨린다.
        return ApprovalResult.created(paymentRepository.saveAndFlush(payment));
    }

    @Transactional
    public Payment cancel(String merchantId, String approvalNo) {
        Payment payment = paymentRepository
                .findByMerchantIdAndApprovalNo(merchantId, approvalNo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "거래를 찾을 수 없습니다: " + approvalNo));
        payment.cancel();
        return payment;
    }

    @Transactional(readOnly = true)
    public Payment inquiry(String merchantId, String approvalNo) {
        return paymentRepository
                .findByMerchantIdAndApprovalNo(merchantId, approvalNo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "거래를 찾을 수 없습니다: " + approvalNo));
    }

    private void validate(String merchantId, String txId,
                          String cardNo, BigDecimal amount) {
        if (merchantId == null || merchantId.isBlank()) {
            throw new IllegalArgumentException("merchantId는 필수입니다");
        }
        if (txId == null || txId.isBlank()) {
            throw new IllegalArgumentException("거래고유번호(txId)는 필수입니다");
        }
        if (cardNo == null || cardNo.length() < 15) {
            throw new IllegalArgumentException("유효하지 않은 카드번호입니다");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("금액은 0보다 커야 합니다");
        }
    }

    private String mask(String cardNo) {
        return cardNo.substring(0, 6) + "******" + cardNo.substring(cardNo.length() - 4);
    }

    private String generateApprovalNo() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return "AP" + date + random;
    }
}
