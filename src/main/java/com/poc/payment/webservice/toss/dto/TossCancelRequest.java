package com.poc.payment.webservice.toss.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TossCancelRequest",
        propOrder = {"storeId", "paymentKey", "cancelReason"})
public class TossCancelRequest {

    @XmlElement(required = true)
    private String storeId;

    @XmlElement(required = true)
    private String paymentKey;

    /** 취소 사유는 이 계약에만 있는 필드다 (카드 계약에는 없다) */
    private String cancelReason;
}
