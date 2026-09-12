package com.poc.payment.webservice.card.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CardInquiryResponse",
        propOrder = {"resultCode", "resultMessage", "approvalNo", "status",
                "maskedCardNo", "amount", "approvedAt", "canceledAt"})
public class CardInquiryResponse {

    private String resultCode;
    private String resultMessage;
    private String approvalNo;
    /** APPROVED / CANCELED */
    private String status;
    private String maskedCardNo;
    private BigDecimal amount;
    private String approvedAt;
    /** 취소되지 않은 거래는 비어 있다 */
    private String canceledAt;
}
