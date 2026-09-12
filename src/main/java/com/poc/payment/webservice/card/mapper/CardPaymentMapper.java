package com.poc.payment.webservice.card.mapper;

import com.poc.payment.domain.Payment;
import com.poc.payment.service.ApprovalResult;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.card.dto.CardCancelResponse;
import com.poc.payment.webservice.card.dto.CardInquiryResponse;

import java.time.format.DateTimeFormatter;

/** 도메인 <-> SOAP DTO 변환 전담. Endpoint를 얇게 유지하기 위한 클래스. */
public final class CardPaymentMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CardPaymentMapper() {
    }

    public static CardApprovalResponse toApprovalResponse(ApprovalResult result) {
        Payment payment = result.payment();
        CardApprovalResponse response = new CardApprovalResponse();
        response.setResultCode("0000");
        response.setResultMessage(result.duplicated()
                ? "이미 승인된 거래입니다 (기존 승인 반환)"
                : "승인 성공");
        response.setApprovalNo(payment.getApprovalNo());
        response.setApprovedAt(payment.getApprovedAt().format(FORMATTER));
        response.setDuplicated(result.duplicated());
        return response;
    }

    public static CardApprovalResponse approvalFail(String code, String message) {
        CardApprovalResponse response = new CardApprovalResponse();
        response.setResultCode(code);
        response.setResultMessage(message);
        return response;
    }

    public static CardCancelResponse toCancelResponse(Payment payment) {
        CardCancelResponse response = new CardCancelResponse();
        response.setResultCode("0000");
        response.setResultMessage("취소 성공");
        response.setApprovalNo(payment.getApprovalNo());
        response.setCanceledAt(payment.getCanceledAt().format(FORMATTER));
        return response;
    }

    public static CardCancelResponse cancelFail(String code, String message) {
        CardCancelResponse response = new CardCancelResponse();
        response.setResultCode(code);
        response.setResultMessage(message);
        return response;
    }

    public static CardInquiryResponse toInquiryResponse(Payment payment) {
        CardInquiryResponse response = new CardInquiryResponse();
        response.setResultCode("0000");
        response.setResultMessage("조회 성공");
        response.setApprovalNo(payment.getApprovalNo());
        response.setStatus(payment.getStatus().name());
        response.setMaskedCardNo(payment.getMaskedCardNo());
        response.setAmount(payment.getAmount());
        response.setApprovedAt(payment.getApprovedAt().format(FORMATTER));
        if (payment.getCanceledAt() != null) {
            response.setCanceledAt(payment.getCanceledAt().format(FORMATTER));
        }
        return response;
    }

    public static CardInquiryResponse inquiryFail(String code, String message) {
        CardInquiryResponse response = new CardInquiryResponse();
        response.setResultCode(code);
        response.setResultMessage(message);
        return response;
    }
}
