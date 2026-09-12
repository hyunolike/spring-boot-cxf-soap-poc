package com.poc.payment.webservice.card.dto;

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
@XmlType(name = "CardInquiryRequest", propOrder = {"merchantId", "approvalNo"})
public class CardInquiryRequest {

    @XmlElement(required = true)
    private String merchantId;

    @XmlElement(required = true)
    private String approvalNo;
}
