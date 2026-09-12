package com.poc.payment.webservice.toss;

import com.poc.payment.domain.Payment;
import com.poc.payment.service.ApprovalResult;
import com.poc.payment.service.PaymentApplicationService;
import com.poc.payment.webservice.toss.dto.TossCancelRequest;
import com.poc.payment.webservice.toss.dto.TossCancelResponse;
import com.poc.payment.webservice.toss.dto.TossPayRequest;
import com.poc.payment.webservice.toss.dto.TossPayResponse;
import com.poc.payment.webservice.toss.mapper.TossPayMapper;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 두 번째 SOAP Adapter.
 * 비즈니스 로직은 여기에도 없다 — 전문 번역만 다르게 하고 같은 서비스에 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@WebService(
        endpointInterface = "com.poc.payment.webservice.toss.TossPayService",
        targetNamespace = TossPayService.NAMESPACE,
        serviceName = "TossPayService",
        portName = "TossPayServicePort"
)
public class TossPayServiceImpl implements TossPayService {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    public TossPayResponse pay(TossPayRequest request) {
        log.info("[SOAP/toss] pay storeId={}, orderId={}, totalAmount={}",
                request.getStoreId(), request.getOrderId(), request.getTotalAmount());
        try {
            ApprovalResult result = paymentApplicationService.approve(
                    request.getStoreId(),
                    request.getOrderId(),   // 이 계약은 주문번호를 멱등성 키로 쓴다
                    request.getPayToken(),
                    request.getTotalAmount());
            return TossPayMapper.toPayResponse(result, request.getOrderId());
        } catch (IllegalArgumentException e) {
            return TossPayMapper.payAborted(request.getOrderId(), e.getMessage());
        } catch (Exception e) {
            log.error("pay failed", e);
            return TossPayMapper.payAborted(request.getOrderId(), "시스템 오류");
        }
    }

    @Override
    public TossCancelResponse cancelPay(TossCancelRequest request) {
        log.info("[SOAP/toss] cancelPay paymentKey={}, reason={}",
                request.getPaymentKey(), request.getCancelReason());
        try {
            Payment payment = paymentApplicationService.cancel(
                    request.getStoreId(),
                    request.getPaymentKey());
            return TossPayMapper.toCancelResponse(payment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return TossPayMapper.cancelAborted(request.getPaymentKey(), e.getMessage());
        } catch (Exception e) {
            log.error("cancelPay failed", e);
            return TossPayMapper.cancelAborted(request.getPaymentKey(), "시스템 오류");
        }
    }
}
