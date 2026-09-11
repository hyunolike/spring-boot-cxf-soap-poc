package com.poc.payment.webservice.card.dto;

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
@XmlType(name = "CardCancelResponse",
        propOrder = {"resultCode", "resultMessage", "approvalNo", "canceledAt"})
public class CardCancelResponse {

    private String resultCode;
    private String resultMessage;
    private String approvalNo;
    private String canceledAt;
}
