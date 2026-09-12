package com.poc.payment;

import com.poc.payment.webservice.card.PaymentService;
import com.poc.payment.webservice.card.dto.CardInquiryRequest;
import com.poc.payment.webservice.card.dto.CardInquiryResponse;
import com.poc.payment.webservice.toss.TossPayService;
import com.poc.payment.webservice.toss.dto.TossCancelRequest;
import com.poc.payment.webservice.toss.dto.TossCancelResponse;
import com.poc.payment.webservice.toss.dto.TossPayRequest;
import com.poc.payment.webservice.toss.dto.TossPayResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 두 번째 Endpoint(/services/toss-payment) 왕복 테스트.
 * 전문 스펙은 다르지만 뒤에 있는 비즈니스 계층은 카드 Endpoint와 같은 것을 쓴다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TossPaySoapIntegrationTest {

    @LocalServerPort
    private int port;

    private TossPayService tossClient() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(TossPayService.class);
        factory.setAddress("http://localhost:" + port + "/services/toss-payment");
        return factory.create(TossPayService.class);
    }

    private PaymentService cardClient() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PaymentService.class);
        factory.setAddress("http://localhost:" + port + "/services/payment");
        return factory.create(PaymentService.class);
    }

    private TossPayRequest payRequest(String orderId, String amount) {
        TossPayRequest request = new TossPayRequest();
        request.setStoreId("STORE-7001");
        request.setOrderId(orderId);
        request.setPayToken("9876543210987654");
        request.setTotalAmount(new BigDecimal(amount));
        return request;
    }

    @Test
    void 결제_후_취소까지_성공한다() {
        TossPayService toss = tossClient();

        TossPayResponse pay = toss.pay(payRequest("ORDER-1", "17000"));
        assertThat(pay.getStatus()).isEqualTo("DONE");
        assertThat(pay.getPaymentKey()).startsWith("AP");
        assertThat(pay.getOrderId()).isEqualTo("ORDER-1");

        TossCancelRequest cancelRequest = new TossCancelRequest();
        cancelRequest.setStoreId("STORE-7001");
        cancelRequest.setPaymentKey(pay.getPaymentKey());
        cancelRequest.setCancelReason("고객 변심");

        TossCancelResponse cancel = toss.cancelPay(cancelRequest);
        assertThat(cancel.getStatus()).isEqualTo("CANCELED");
    }

    @Test
    void 잘못된_금액이면_ABORTED_상태를_반환한다() {
        TossPayService toss = tossClient();

        TossPayResponse pay = toss.pay(payRequest("ORDER-2", "-1"));
        assertThat(pay.getStatus()).isEqualTo("ABORTED");
        assertThat(pay.getPaymentKey()).isNull();
    }

    @Test
    void 이미_취소된_결제를_다시_취소하면_ABORTED_상태를_반환한다() {
        TossPayService toss = tossClient();

        TossPayResponse pay = toss.pay(payRequest("ORDER-3", "5000"));

        TossCancelRequest cancelRequest = new TossCancelRequest();
        cancelRequest.setStoreId("STORE-7001");
        cancelRequest.setPaymentKey(pay.getPaymentKey());
        toss.cancelPay(cancelRequest);

        TossCancelResponse second = toss.cancelPay(cancelRequest);
        assertThat(second.getStatus()).isEqualTo("ABORTED");
        assertThat(second.getMessage()).contains("이미 취소된 거래");
    }

    /** 멀티 Endpoint의 핵심: 한쪽으로 넣은 거래를 다른 쪽 계약으로 조회할 수 있다. */
    @Test
    void 토스_계약으로_승인한_거래를_카드_계약으로_조회할_수_있다() {
        TossPayResponse pay = tossClient().pay(payRequest("ORDER-4", "42000"));

        CardInquiryRequest inquiryRequest = new CardInquiryRequest();
        inquiryRequest.setMerchantId("STORE-7001");
        inquiryRequest.setApprovalNo(pay.getPaymentKey());

        CardInquiryResponse inquiry = cardClient().inquiry(inquiryRequest);
        assertThat(inquiry.getResultCode()).isEqualTo("0000");
        assertThat(inquiry.getStatus()).isEqualTo("APPROVED");
        assertThat(inquiry.getAmount()).isEqualByComparingTo(new BigDecimal("42000"));
        assertThat(inquiry.getMaskedCardNo()).isEqualTo("987654******7654");
    }
}
