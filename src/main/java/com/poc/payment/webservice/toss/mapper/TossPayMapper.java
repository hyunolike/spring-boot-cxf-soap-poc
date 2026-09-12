package com.poc.payment.webservice.toss.mapper;

import com.poc.payment.domain.Payment;
import com.poc.payment.service.ApprovalResult;
import com.poc.payment.webservice.toss.dto.TossCancelResponse;
import com.poc.payment.webservice.toss.dto.TossPayResponse;

import java.time.format.DateTimeFormatter;

/**
 * 같은 도메인을 카드 계약과 다른 어휘로 번역한다.
 * approvalNo -> paymentKey, 결과 코드 -> 상태 문자열.
 */
public final class TossPayMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private TossPayMapper() {
    }

    public static TossPayResponse toPayResponse(ApprovalResult result, String orderId) {
        Payment payment = result.payment();
        TossPayResponse response = new TossPayResponse();
        // 멱등 응답이므로 재요청도 성공 상태로 내려준다.
        response.setStatus("DONE");
        response.setPaymentKey(payment.getApprovalNo());
        response.setOrderId(orderId);
        response.setMessage(result.duplicated() ? "이미 처리된 결제입니다" : "결제 완료");
        response.setApprovedAt(payment.getApprovedAt().format(FORMATTER));
        response.setDuplicated(result.duplicated());
        return response;
    }

    public static TossPayResponse payAborted(String orderId, String message) {
        TossPayResponse response = new TossPayResponse();
        response.setStatus("ABORTED");
        response.setOrderId(orderId);
        response.setMessage(message);
        return response;
    }

    public static TossCancelResponse toCancelResponse(Payment payment) {
        TossCancelResponse response = new TossCancelResponse();
        response.setStatus("CANCELED");
        response.setPaymentKey(payment.getApprovalNo());
        response.setMessage("결제 취소 완료");
        response.setCanceledAt(payment.getCanceledAt().format(FORMATTER));
        return response;
    }

    public static TossCancelResponse cancelAborted(String paymentKey, String message) {
        TossCancelResponse response = new TossCancelResponse();
        response.setStatus("ABORTED");
        response.setPaymentKey(paymentKey);
        response.setMessage(message);
        return response;
    }
}
