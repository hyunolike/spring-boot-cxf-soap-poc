package com.poc.payment.webservice.toss.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 카드 계약은 결과 코드(0000)를, 이 계약은 상태 문자열(DONE)을 쓴다. */
@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TossPayResponse",
        propOrder = {"status", "paymentKey", "orderId", "message", "approvedAt"})
public class TossPayResponse {

    /** DONE / ABORTED */
    private String status;
    /** 내부 승인번호를 외부에는 paymentKey로 노출한다 */
    private String paymentKey;
    private String orderId;
    private String message;
    private String approvedAt;
}
