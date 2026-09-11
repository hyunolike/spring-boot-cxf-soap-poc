package com.poc.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String merchantId;

    /** 카드번호는 마스킹된 형태로만 저장 */
    @Column(nullable = false)
    private String maskedCardNo;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @Column(nullable = false, unique = true)
    private String approvalNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private LocalDateTime approvedAt;

    private LocalDateTime canceledAt;

    public static Payment approve(String merchantId, String maskedCardNo,
                                  BigDecimal amount, String approvalNo) {
        Payment payment = new Payment();
        payment.merchantId = merchantId;
        payment.maskedCardNo = maskedCardNo;
        payment.amount = amount;
        payment.approvalNo = approvalNo;
        payment.status = PaymentStatus.APPROVED;
        payment.approvedAt = LocalDateTime.now();
        return payment;
    }

    public void cancel() {
        if (this.status == PaymentStatus.CANCELED) {
            throw new IllegalStateException("이미 취소된 거래입니다: " + approvalNo);
        }
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }
}
