package com.poc.payment.webservice.toss.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TossCancelResponse",
        propOrder = {"status", "paymentKey", "message", "canceledAt"})
public class TossCancelResponse {

    /** CANCELED / ABORTED */
    private String status;
    private String paymentKey;
    private String message;
    private String canceledAt;
}
