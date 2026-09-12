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
        propOrder = {"resultCode", "resultMessage", "approvalNo", "approvedAt", "duplicated"})
public class CardApprovalResponse {

    /** 0000: 성공 */
    private String resultCode;
    private String resultMessage;
    private String approvalNo;
    private String approvedAt;
    /** true면 이번 요청으로 승인된 것이 아니라 기존 승인을 그대로 돌려준 것이다. */
    private boolean duplicated;
}
