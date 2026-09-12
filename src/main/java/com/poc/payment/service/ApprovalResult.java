package com.poc.payment.service;

import com.poc.payment.domain.Payment;

/**
 * 승인 결과 + "이번 요청으로 새로 승인된 것인지" 여부.
 * 어댑터가 중복 요청을 외부 계약의 언어로 표현할 수 있게 비즈니스 계층이 알려준다.
 */
public record ApprovalResult(Payment payment, boolean duplicated) {

    public static ApprovalResult created(Payment payment) {
        return new ApprovalResult(payment, false);
    }

    public static ApprovalResult duplicated(Payment payment) {
        return new ApprovalResult(payment, true);
    }
}
