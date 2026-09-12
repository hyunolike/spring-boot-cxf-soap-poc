package com.poc.payment.webservice.card.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** SOAP 요청 메시지 계약. JAXB가 XML <-> 객체 변환을 담당한다. */
@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardApprovalRequest", propOrder = {"merchantId", "txId", "cardNo", "amount"})
public class CardApprovalRequest {

    @XmlElement(required = true)
    private String merchantId;

    /** 가맹점 거래고유번호. 같은 값으로 두 번 보내도 승인은 한 건만 생긴다. */
    @XmlElement(required = true)
    private String txId;

    @XmlElement(required = true)
    private String cardNo;

    @XmlElement(required = true)
    private BigDecimal amount;
}
