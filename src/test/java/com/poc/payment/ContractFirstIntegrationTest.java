package com.poc.payment;

import com.poc.payment.generated.cardv1.ApproveCardRequest;
import com.poc.payment.generated.cardv1.ApproveCardResponse;
import com.poc.payment.generated.cardv1.CardPaymentPortType;
import com.poc.payment.generated.cardv1.InquireCardRequest;
import com.poc.payment.generated.cardv1.InquireCardResponse;
import com.poc.payment.generated.cardv1.PaymentStatus;
import com.poc.payment.support.SoapTestClients;
import jakarta.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contract First Endpoint 왕복 테스트.
 * 구현하는 SEI가 손으로 쓴 것이 아니라 wsdl2java 산출물이라는 점만 빼면
 * Java First Endpoint와 쓰는 법은 같다. 차이는 계약이 무엇을 강제하는가에서 나온다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContractFirstIntegrationTest {

    private static final String PATH = "/services/card-payment-v1";

    @LocalServerPort
    private int port;

    private CardPaymentPortType client() {
        return SoapTestClients.secured(CardPaymentPortType.class, port, PATH);
    }

    private ApproveCardRequest approveRequest(String txId, String amount) {
        ApproveCardRequest request = new ApproveCardRequest();
        request.setMerchantId("M3001");
        request.setTxId(txId);
        request.setCardNo("1234567890123456");
        request.setAmount(new BigDecimal(amount));
        return request;
    }

    @Test
    void 승인하면_계약이_정의한_타입으로_응답한다() {
        ApproveCardResponse response = client().approveCard(approveRequest("TX-CF-1", "11000"));

        assertThat(response.getResultCode()).isEqualTo("0000");
        assertThat(response.getApprovalNo()).matches("AP[0-9]{14}");
        assertThat(response.isDuplicated()).isFalse();
        // 문자열이 아니라 xs:dateTime으로 내려온다.
        assertThat(response.getApprovedAt()).isNotNull();
        assertThat(response.getApprovedAt().getYear()).isGreaterThan(2000);
    }

    @Test
    void 조회하면_상태가_스키마_enum으로_내려온다() {
        CardPaymentPortType card = client();
        ApproveCardResponse approval = card.approveCard(approveRequest("TX-CF-2", "13000"));

        InquireCardRequest request = new InquireCardRequest();
        request.setMerchantId("M3001");
        request.setApprovalNo(approval.getApprovalNo());

        InquireCardResponse response = card.inquireCard(request);
        assertThat(response.getResultCode()).isEqualTo("0000");
        // String이 아니라 생성된 enum 타입이다 — 오타가 컴파일 단계에서 걸린다.
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("13000"));
        assertThat(response.getMaskedCardNo()).isEqualTo("123456******3456");
    }

    /** 멱등성은 비즈니스 계층에 있으므로 어느 계약으로 들어와도 똑같이 동작한다. */
    @Test
    void 같은_거래고유번호로_두_번_승인하면_기존_승인을_돌려준다() {
        CardPaymentPortType card = client();

        ApproveCardResponse first = card.approveCard(approveRequest("TX-CF-DUP", "7000"));
        ApproveCardResponse second = card.approveCard(approveRequest("TX-CF-DUP", "7000"));

        assertThat(second.isDuplicated()).isTrue();
        assertThat(second.getApprovalNo()).isEqualTo(first.getApprovalNo());
    }

    /**
     * Contract First의 실질적인 이득.
     * Java First Endpoint는 음수 금액을 비즈니스 검증으로 잡아 결과 코드 1001을 주지만,
     * 여기서는 스키마(minExclusive=0)가 비즈니스 코드에 닿기도 전에 Fault로 막는다.
     */
    @Test
    void 계약을_위반한_금액은_비즈니스_코드에_닿기도_전에_막힌다() {
        CardPaymentPortType card = client();

        assertThatThrownBy(() -> card.approveCard(approveRequest("TX-CF-INVALID", "-100")))
                .isInstanceOf(SOAPFaultException.class)
                .hasMessageContaining("minExclusive");
    }

    /** 손으로 쓴 WSDL이 그대로 publish되는지 — 생성 WSDL이 아니라 원본이어야 한다. */
    @Test
    void publish된_WSDL은_손으로_쓴_원본이다() throws IOException, InterruptedException {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:" + port + PATH + "?wsdl"))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        // Java First로는 만들 수 없던 제약들이 publish된 계약에 살아 있다.
        assertThat(response.body())
                .contains("name=\"CardPaymentServiceV1\"")
                .contains("AP[0-9]{14}")
                .contains("minExclusive")
                .contains("<xs:enumeration value=\"APPROVED\"/>");
    }
}
