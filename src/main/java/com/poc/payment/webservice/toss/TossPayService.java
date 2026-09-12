package com.poc.payment.webservice.toss;

import com.poc.payment.webservice.toss.dto.TossCancelRequest;
import com.poc.payment.webservice.toss.dto.TossCancelResponse;
import com.poc.payment.webservice.toss.dto.TossPayRequest;
import com.poc.payment.webservice.toss.dto.TossPayResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;

/**
 * 두 번째 SEI = 두 번째 외부 계약.
 * PaymentService(카드사)와 전문 스펙도, 네임스페이스도, Endpoint 주소도 다르지만
 * 뒤에 있는 PaymentApplicationService는 똑같이 재사용한다.
 */
@WebService(
        name = "TossPayService",
        targetNamespace = TossPayService.NAMESPACE
)
public interface TossPayService {

    String NAMESPACE = "http://toss.payment.poc.com/";

    @WebMethod
    @WebResult(name = "response")
    TossPayResponse pay(@WebParam(name = "request") TossPayRequest request);

    @WebMethod
    @WebResult(name = "response")
    TossCancelResponse cancelPay(@WebParam(name = "request") TossCancelRequest request);
}
