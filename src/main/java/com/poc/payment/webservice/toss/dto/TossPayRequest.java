package com.poc.payment.webservice.toss.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 같은 비즈니스(카드 승인)를 전혀 다른 전문 스펙으로 받는다.
 * merchantId -> storeId, cardNo -> payToken, amount -> totalAmount.
 */
@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TossPayRequest",
        propOrder = {"storeId", "orderId", "payToken", "totalAmount"})
public class TossPayRequest {

    @XmlElement(required = true)
    private String storeId;

    /** 가맹점이 부여하는 주문번호 */
    @XmlElement(required = true)
    private String orderId;

    /** 카드번호를 대신하는 결제 토큰 */
    @XmlElement(required = true)
    private String payToken;

    @XmlElement(required = true)
    private BigDecimal totalAmount;
}
