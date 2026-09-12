package com.poc.payment.webservice.card;

import com.poc.payment.webservice.card.dto.CardApprovalRequest;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.card.dto.CardCancelRequest;
import com.poc.payment.webservice.card.dto.CardCancelResponse;
import com.poc.payment.webservice.card.dto.CardInquiryRequest;
import com.poc.payment.webservice.card.dto.CardInquiryResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * SEI (Service Endpoint Interface).
 * 일반 Spring @Service가 아니라 외부 시스템과의 SOAP 계약(Contract)이다.
 * 이 인터페이스가 그대로 WSDL로 노출된다 (Java First / Code First 방식).
 */
@WebService(
        name = "PaymentService",
        targetNamespace = PaymentService.NAMESPACE
)
public interface PaymentService {

    String NAMESPACE = "http://payment.poc.com/";

    @WebMethod
    @WebResult(name = "response")
    CardApprovalResponse approve(
            @WebParam(name = "request") CardApprovalRequest request);

    @WebMethod
    @WebResult(name = "response")
    CardCancelResponse cancel(
            @WebParam(name = "request") CardCancelRequest request);

    /** 오퍼레이션을 하나 늘리면 WSDL이 어떻게 늘어나는지 관찰하기 위한 조회. */
    @WebMethod
    @WebResult(name = "response")
    CardInquiryResponse inquiry(
            @WebParam(name = "request") CardInquiryRequest request);
}
