package com.poc.payment.webservice.card;

import com.poc.payment.domain.Payment;
import com.poc.payment.service.PaymentApplicationService;
import com.poc.payment.webservice.card.dto.CardApprovalRequest;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.card.dto.CardCancelRequest;
import com.poc.payment.webservice.card.dto.CardCancelResponse;
import com.poc.payment.webservice.card.mapper.CardPaymentMapper;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SOAP Endpoint 구현체 = SOAP Adapter.
 * 여기에는 비즈니스 로직을 두지 않는다.
 * DTO <-> 도메인 변환 후 ApplicationService에 위임만 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@WebService(
        endpointInterface = "com.poc.payment.webservice.card.PaymentService",
        targetNamespace = PaymentService.NAMESPACE,
        serviceName = "PaymentService",
        portName = "PaymentServicePort"
)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    public CardApprovalResponse approve(CardApprovalRequest request) {
        log.info("[SOAP] approve merchantId={}, amount={}",
                request.getMerchantId(), request.getAmount());
        try {
            Payment payment = paymentApplicationService.approve(
                    request.getMerchantId(),
                    request.getCardNo(),
                    request.getAmount());
            return CardPaymentMapper.toApprovalResponse(payment);
        } catch (IllegalArgumentException e) {
            return CardPaymentMapper.approvalFail("1001", e.getMessage());
        } catch (Exception e) {
            log.error("approve failed", e);
            return CardPaymentMapper.approvalFail("9999", "시스템 오류");
        }
    }

    @Override
    public CardCancelResponse cancel(CardCancelRequest request) {
        log.info("[SOAP] cancel approvalNo={}", request.getApprovalNo());
        try {
            Payment payment = paymentApplicationService.cancel(
                    request.getMerchantId(),
                    request.getApprovalNo());
            return CardPaymentMapper.toCancelResponse(payment);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CardPaymentMapper.cancelFail("2001", e.getMessage());
        } catch (Exception e) {
            log.error("cancel failed", e);
            return CardPaymentMapper.cancelFail("9999", "시스템 오류");
        }
    }
}
