package com.poc.payment;

import com.poc.payment.webservice.card.PaymentService;
import com.poc.payment.webservice.card.dto.CardApprovalRequest;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.card.dto.CardCancelRequest;
import com.poc.payment.webservice.card.dto.CardCancelResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 진짜 SOAP(HTTP + XML) 왕복 테스트.
 * JaxWsProxyFactoryBean으로 SEI 기반 동적 클라이언트를 만들어 호출한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentSoapIntegrationTest {

    @LocalServerPort
    private int port;

    private PaymentService client() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PaymentService.class);
        factory.setAddress("http://localhost:" + port + "/services/payment");
        return factory.create(PaymentService.class);
    }

    @Test
    void 승인_후_취소까지_성공한다() {
        PaymentService payment = client();

        CardApprovalRequest approvalRequest = new CardApprovalRequest();
        approvalRequest.setMerchantId("M1001");
        approvalRequest.setCardNo("1234567890123456");
        approvalRequest.setAmount(new BigDecimal("10000"));

        CardApprovalResponse approval = payment.approve(approvalRequest);
        assertThat(approval.getResultCode()).isEqualTo("0000");
        assertThat(approval.getApprovalNo()).startsWith("AP");

        CardCancelRequest cancelRequest = new CardCancelRequest();
        cancelRequest.setMerchantId("M1001");
        cancelRequest.setApprovalNo(approval.getApprovalNo());

        CardCancelResponse cancel = payment.cancel(cancelRequest);
        assertThat(cancel.getResultCode()).isEqualTo("0000");
    }

    @Test
    void 잘못된_금액이면_실패코드를_반환한다() {
        PaymentService payment = client();

        CardApprovalRequest request = new CardApprovalRequest();
        request.setMerchantId("M1001");
        request.setCardNo("1234567890123456");
        request.setAmount(new BigDecimal("-100"));

        CardApprovalResponse response = payment.approve(request);
        assertThat(response.getResultCode()).isEqualTo("1001");
    }
}
