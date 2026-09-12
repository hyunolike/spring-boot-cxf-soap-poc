package com.poc.payment;

import com.poc.payment.support.SoapTestClients;
import com.poc.payment.webservice.card.PaymentService;
import com.poc.payment.webservice.card.dto.CardApprovalRequest;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.toss.TossPayService;
import com.poc.payment.webservice.toss.dto.TossPayRequest;
import jakarta.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SOAP 헤더 인증(WS-Security UsernameToken) 검증.
 * 인증 실패는 결과 코드가 아니라 SOAP Fault로 나간다 —
 * 전문 오류(비즈니스)와 인증 오류(프로토콜)는 층이 다르기 때문이다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WsSecurityIntegrationTest {

    private static final String CARD_PATH = "/services/payment";

    @LocalServerPort
    private int port;

    private CardApprovalRequest approvalRequest(String txId) {
        CardApprovalRequest request = new CardApprovalRequest();
        request.setMerchantId("M1001");
        request.setTxId(txId);
        request.setCardNo("1234567890123456");
        request.setAmount(new BigDecimal("10000"));
        return request;
    }

    @Test
    void 올바른_UsernameToken이면_승인된다() {
        PaymentService payment =
                SoapTestClients.secured(PaymentService.class, port, CARD_PATH);

        CardApprovalResponse response = payment.approve(approvalRequest("TX-SEC-OK"));
        assertThat(response.getResultCode()).isEqualTo("0000");
    }

    @Test
    void 인증_헤더가_없으면_Fault가_떨어진다() {
        PaymentService payment =
                SoapTestClients.plain(PaymentService.class, port, CARD_PATH);

        assertThatThrownBy(() -> payment.approve(approvalRequest("TX-SEC-NONE")))
                .isInstanceOf(SOAPFaultException.class)
                .hasMessageContaining("security error");
    }

    @Test
    void 비밀번호가_틀리면_Fault가_떨어진다() {
        PaymentService payment = SoapTestClients.secured(
                PaymentService.class, port, CARD_PATH, "poc-client", "wrong-secret");

        assertThatThrownBy(() -> payment.approve(approvalRequest("TX-SEC-BADPW")))
                .isInstanceOf(SOAPFaultException.class)
                .hasMessageContaining("security error");
    }

    @Test
    void 등록되지_않은_사용자면_Fault가_떨어진다() {
        PaymentService payment = SoapTestClients.secured(
                PaymentService.class, port, CARD_PATH, "stranger", "poc-secret");

        assertThatThrownBy(() -> payment.approve(approvalRequest("TX-SEC-UNKNOWN")))
                .isInstanceOf(SOAPFaultException.class)
                .hasMessageContaining("security error");
    }

    /** 인증은 Endpoint마다 붙인다 — 두 번째 Endpoint도 같이 막혀 있어야 한다. */
    @Test
    void 토스_Endpoint도_인증_없이는_호출할_수_없다() {
        TossPayService toss =
                SoapTestClients.plain(TossPayService.class, port, "/services/toss-payment");

        TossPayRequest request = new TossPayRequest();
        request.setStoreId("STORE-7001");
        request.setOrderId("ORDER-SEC-NONE");
        request.setPayToken("9876543210987654");
        request.setTotalAmount(new BigDecimal("1000"));

        assertThatThrownBy(() -> toss.pay(request))
                .isInstanceOf(SOAPFaultException.class)
                .hasMessageContaining("security error");
    }
}
