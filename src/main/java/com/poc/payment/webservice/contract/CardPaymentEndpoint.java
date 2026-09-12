package com.poc.payment.webservice.contract;

import com.poc.payment.domain.Payment;
import com.poc.payment.generated.cardv1.ApproveCardRequest;
import com.poc.payment.generated.cardv1.ApproveCardResponse;
import com.poc.payment.generated.cardv1.CardPaymentPortType;
import com.poc.payment.generated.cardv1.InquireCardRequest;
import com.poc.payment.generated.cardv1.InquireCardResponse;
import com.poc.payment.service.ApprovalResult;
import com.poc.payment.service.PaymentApplicationService;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Contract First Endpoint.
 *
 * Java First(PaymentServiceImpl)와의 차이는 "누가 계약의 주인인가"다.
 *   - Java First  : SEI를 고치면 WSDL이 따라 바뀐다 (계약이 코드에 끌려다닌다)
 *   - Contract First: WSDL을 고치면 생성 코드가 바뀌고 컴파일이 깨진다 (코드가 계약에 끌려온다)
 *
 * 구현하는 인터페이스(CardPaymentPortType)는 손으로 쓴 게 아니라 wsdl2java 산출물이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@WebService(
        endpointInterface = "com.poc.payment.generated.cardv1.CardPaymentPortType",
        targetNamespace = "http://contract.payment.poc.com/v1",
        serviceName = "CardPaymentService",
        portName = "CardPaymentPort"
)
public class CardPaymentEndpoint implements CardPaymentPortType {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    public ApproveCardResponse approveCard(ApproveCardRequest request) {
        log.info("[SOAP/contract-first] approveCard merchantId={}, txId={}",
                request.getMerchantId(), request.getTxId());
        try {
            ApprovalResult result = paymentApplicationService.approve(
                    request.getMerchantId(),
                    request.getTxId(),
                    request.getCardNo(),
                    request.getAmount());
            return ContractCardMapper.toApproveResponse(result);
        } catch (IllegalArgumentException e) {
            return ContractCardMapper.approveFail("1001", e.getMessage());
        } catch (Exception e) {
            log.error("approveCard failed", e);
            return ContractCardMapper.approveFail("9999", "시스템 오류");
        }
    }

    @Override
    public InquireCardResponse inquireCard(InquireCardRequest request) {
        log.info("[SOAP/contract-first] inquireCard approvalNo={}", request.getApprovalNo());
        try {
            Payment payment = paymentApplicationService.inquiry(
                    request.getMerchantId(),
                    request.getApprovalNo());
            return ContractCardMapper.toInquireResponse(payment);
        } catch (IllegalArgumentException e) {
            return ContractCardMapper.inquireFail("3001", e.getMessage());
        } catch (Exception e) {
            log.error("inquireCard failed", e);
            return ContractCardMapper.inquireFail("9999", "시스템 오류");
        }
    }
}
