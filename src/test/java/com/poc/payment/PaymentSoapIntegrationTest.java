package com.poc.payment;

import com.poc.payment.webservice.card.PaymentService;
import com.poc.payment.webservice.card.dto.CardApprovalRequest;
import com.poc.payment.webservice.card.dto.CardApprovalResponse;
import com.poc.payment.webservice.card.dto.CardCancelRequest;
import com.poc.payment.webservice.card.dto.CardCancelResponse;
import com.poc.payment.webservice.card.dto.CardInquiryRequest;
import com.poc.payment.webservice.card.dto.CardInquiryResponse;
import com.poc.payment.support.SoapTestClients;
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

    private static final String MERCHANT_ID = "M1001";
    private static final String CARD_NO = "1234567890123456";

    @LocalServerPort
    private int port;

    private PaymentService client() {
        return SoapTestClients.secured(PaymentService.class, port, "/services/payment");
    }

    private CardApprovalRequest approvalRequest(String txId, String amount) {
        CardApprovalRequest request = new CardApprovalRequest();
        request.setMerchantId(MERCHANT_ID);
        request.setTxId(txId);
        request.setCardNo(CARD_NO);
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    private CardInquiryRequest inquiryRequest(String approvalNo) {
        CardInquiryRequest request = new CardInquiryRequest();
        request.setMerchantId(MERCHANT_ID);
        request.setApprovalNo(approvalNo);
        return request;
    }

    private CardCancelRequest cancelRequest(String approvalNo) {
        CardCancelRequest request = new CardCancelRequest();
        request.setMerchantId(MERCHANT_ID);
        request.setApprovalNo(approvalNo);
        return request;
    }

    @Test
    void 승인_후_취소까지_성공한다() {
        PaymentService payment = client();

        CardApprovalResponse approval = payment.approve(approvalRequest("TX-CARD-1", "10000"));
        assertThat(approval.getResultCode()).isEqualTo("0000");
        assertThat(approval.getApprovalNo()).startsWith("AP");
        assertThat(approval.isDuplicated()).isFalse();

        CardCancelResponse cancel = payment.cancel(cancelRequest(approval.getApprovalNo()));
        assertThat(cancel.getResultCode()).isEqualTo("0000");
    }

    @Test
    void 승인한_거래를_조회하면_상태와_금액이_내려온다() {
        PaymentService payment = client();

        CardApprovalResponse approval = payment.approve(approvalRequest("TX-CARD-2", "25000"));

        CardInquiryResponse inquiry = payment.inquiry(inquiryRequest(approval.getApprovalNo()));
        assertThat(inquiry.getResultCode()).isEqualTo("0000");
        assertThat(inquiry.getStatus()).isEqualTo("APPROVED");
        assertThat(inquiry.getAmount()).isEqualByComparingTo(new BigDecimal("25000"));
        assertThat(inquiry.getMaskedCardNo()).isEqualTo("123456******3456");
        assertThat(inquiry.getCanceledAt()).isNull();
    }

    @Test
    void 취소한_거래를_조회하면_취소상태가_내려온다() {
        PaymentService payment = client();

        CardApprovalResponse approval = payment.approve(approvalRequest("TX-CARD-3", "30000"));
        payment.cancel(cancelRequest(approval.getApprovalNo()));

        CardInquiryResponse inquiry = payment.inquiry(inquiryRequest(approval.getApprovalNo()));
        assertThat(inquiry.getStatus()).isEqualTo("CANCELED");
        assertThat(inquiry.getCanceledAt()).isNotBlank();
    }

    @Test
    void 없는_승인번호를_조회하면_실패코드를_반환한다() {
        CardInquiryResponse response = client().inquiry(inquiryRequest("AP20990101000000"));
        assertThat(response.getResultCode()).isEqualTo("3001");
    }

    @Test
    void 잘못된_금액이면_실패코드를_반환한다() {
        CardApprovalResponse response = client().approve(approvalRequest("TX-CARD-4", "-100"));
        assertThat(response.getResultCode()).isEqualTo("1001");
    }

    @Test
    void 거래고유번호가_없으면_실패코드를_반환한다() {
        CardApprovalRequest request = approvalRequest(null, "10000");

        CardApprovalResponse response = client().approve(request);
        assertThat(response.getResultCode()).isEqualTo("1001");
        assertThat(response.getResultMessage()).contains("거래고유번호");
    }

    @Test
    void 같은_거래고유번호로_두_번_승인해도_승인은_한_건만_생긴다() {
        PaymentService payment = client();

        CardApprovalResponse first = payment.approve(approvalRequest("TX-CARD-DUP", "12000"));
        CardApprovalResponse second = payment.approve(approvalRequest("TX-CARD-DUP", "12000"));

        assertThat(first.isDuplicated()).isFalse();
        assertThat(second.isDuplicated()).isTrue();
        assertThat(second.getResultCode()).isEqualTo("0000");
        // 새 승인번호를 발급하지 않고 기존 승인을 그대로 돌려준다.
        assertThat(second.getApprovalNo()).isEqualTo(first.getApprovalNo());
        assertThat(second.getApprovedAt()).isEqualTo(first.getApprovedAt());
    }

    @Test
    void 거래고유번호가_다르면_별도_승인으로_처리된다() {
        PaymentService payment = client();

        CardApprovalResponse first = payment.approve(approvalRequest("TX-CARD-5", "1000"));
        CardApprovalResponse second = payment.approve(approvalRequest("TX-CARD-6", "1000"));

        assertThat(second.isDuplicated()).isFalse();
        assertThat(second.getApprovalNo()).isNotEqualTo(first.getApprovalNo());
    }

    @Test
    void 금액이_달라도_같은_거래고유번호면_기존_승인을_돌려준다() {
        PaymentService payment = client();

        CardApprovalResponse first = payment.approve(approvalRequest("TX-CARD-7", "9000"));
        CardApprovalResponse retry = payment.approve(approvalRequest("TX-CARD-7", "99000"));

        assertThat(retry.isDuplicated()).isTrue();
        assertThat(retry.getApprovalNo()).isEqualTo(first.getApprovalNo());

        // 금액이 덮어써지지 않았는지 확인한다.
        CardInquiryResponse inquiry = payment.inquiry(inquiryRequest(first.getApprovalNo()));
        assertThat(inquiry.getAmount()).isEqualByComparingTo(new BigDecimal("9000"));
    }
}
