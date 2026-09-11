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
@XmlType(name = "CardApprovalResponse",
        propOrder = {"resultCode", "resultMessage", "approvalNo", "approvedAt"})
public class CardApprovalResponse {

    /** 0000: 성공 */
    private String resultCode;
    private String resultMessage;
    private String approvalNo;
    private String approvedAt;
}
