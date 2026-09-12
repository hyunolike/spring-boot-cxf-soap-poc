package com.poc.payment.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SOAP 헤더 인증에 쓰는 계정.
 * PoC라서 설정 파일에 한 쌍만 두지만, 실전이라면 계정 저장소를 보게 된다.
 */
@ConfigurationProperties(prefix = "soap.security")
public record SoapSecurityProperties(String username, String password) {
}
