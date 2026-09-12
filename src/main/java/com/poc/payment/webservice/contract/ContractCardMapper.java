package com.poc.payment.webservice.contract;

import com.poc.payment.domain.Payment;
import com.poc.payment.generated.cardv1.ApproveCardResponse;
import com.poc.payment.generated.cardv1.InquireCardResponse;
import com.poc.payment.generated.cardv1.PaymentStatus;
import com.poc.payment.service.ApprovalResult;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;

/**
 * 도메인 -> Contract First 생성 타입 변환.
 *
 * Java First 매퍼와 비교해서 보면 차이가 드러난다.
 *   - 시각: 문자열 포매팅이 아니라 xs:dateTime(XMLGregorianCalendar)으로 넘긴다
 *   - 상태: 문자열이 아니라 스키마가 정의한 enum으로 넘긴다
 * 계약이 타입을 정해두면 어댑터가 포맷을 발명할 자리가 없다.
 */
final class ContractCardMapper {

    private static final DatatypeFactory DATATYPE_FACTORY = createDatatypeFactory();

    private ContractCardMapper() {
    }

    static ApproveCardResponse toApproveResponse(ApprovalResult result) {
        Payment payment = result.payment();
        ApproveCardResponse response = new ApproveCardResponse();
        response.setResultCode("0000");
        response.setResultMessage(result.duplicated()
                ? "이미 승인된 거래입니다 (기존 승인 반환)"
                : "승인 성공");
        response.setApprovalNo(payment.getApprovalNo());
        response.setApprovedAt(toXmlDateTime(payment.getApprovedAt()));
        response.setDuplicated(result.duplicated());
        return response;
    }

    static ApproveCardResponse approveFail(String code, String message) {
        ApproveCardResponse response = new ApproveCardResponse();
        response.setResultCode(code);
        response.setResultMessage(message);
        return response;
    }

    static InquireCardResponse toInquireResponse(Payment payment) {
        InquireCardResponse response = new InquireCardResponse();
        response.setResultCode("0000");
        response.setResultMessage("조회 성공");
        response.setApprovalNo(payment.getApprovalNo());
        response.setStatus(PaymentStatus.valueOf(payment.getStatus().name()));
        response.setMaskedCardNo(payment.getMaskedCardNo());
        response.setAmount(payment.getAmount());
        response.setApprovedAt(toXmlDateTime(payment.getApprovedAt()));
        if (payment.getCanceledAt() != null) {
            response.setCanceledAt(toXmlDateTime(payment.getCanceledAt()));
        }
        return response;
    }

    static InquireCardResponse inquireFail(String code, String message) {
        InquireCardResponse response = new InquireCardResponse();
        response.setResultCode(code);
        response.setResultMessage(message);
        return response;
    }

    /**
     * xs:dateTime의 대가. 기본 JAXB 바인딩은 XMLGregorianCalendar를 주므로
     * LocalDateTime을 매번 변환해야 한다.
     * 실전에서는 바인딩 커스터마이징(.xjb + XmlAdapter)으로 이 변환을 없앤다.
     */
    private static XMLGregorianCalendar toXmlDateTime(LocalDateTime dateTime) {
        return DATATYPE_FACTORY.newXMLGregorianCalendar(
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                0,
                DatatypeConstants.FIELD_UNDEFINED);   // 타임존 없이 로컬 시각으로 보낸다
    }

    private static DatatypeFactory createDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("DatatypeFactory를 만들 수 없습니다", e);
        }
    }
}
