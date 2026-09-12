package com.poc.payment.config;

import com.poc.payment.security.SoapSecurityProperties;
import com.poc.payment.webservice.contract.CardPaymentEndpoint;
import com.poc.payment.security.UsernameTokenCallbackHandler;
import com.poc.payment.webservice.card.PaymentServiceImpl;
import com.poc.payment.webservice.toss.TossPayServiceImpl;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * SOAP Endpoint 등록.
 * cxf.path(application.yml) = /services 이므로 최종 주소는
 *   카드 Endpoint : http://localhost:8080/services/payment         (?wsdl)
 *   토스 Endpoint : http://localhost:8080/services/toss-payment    (?wsdl)
 *   계약 Endpoint : http://localhost:8080/services/card-payment-v1 (?wsdl) — Contract First
 *
 * 하나의 Bus에 SEI마다 Endpoint를 따로 publish한다.
 * 네임스페이스가 서로 달라야 WSDL/JAXB 컨텍스트가 충돌하지 않는다.
 */
@Configuration
@EnableConfigurationProperties(SoapSecurityProperties.class)
public class CxfConfig {

    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature feature = new LoggingFeature();
        feature.setPrettyLogging(true);
        return feature;
    }

    /**
     * SOAP 헤더(wsse:Security)의 UsernameToken을 검증하는 In 인터셉터.
     * Body를 JAXB로 언마샬링하기 전에 헤더 인증이 먼저 끝난다.
     */
    @Bean
    public WSS4JInInterceptor usernameTokenInterceptor(UsernameTokenCallbackHandler callbackHandler) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        properties.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        properties.put(ConfigurationConstants.PW_CALLBACK_REF, callbackHandler);
        return new WSS4JInInterceptor(properties);
    }

    @Bean
    public Endpoint paymentEndpoint(Bus bus,
                                    PaymentServiceImpl paymentService,
                                    LoggingFeature loggingFeature,
                                    WSS4JInInterceptor usernameTokenInterceptor) {
        EndpointImpl endpoint = new EndpointImpl(bus, paymentService);
        endpoint.getFeatures().add(loggingFeature);
        endpoint.getInInterceptors().add(usernameTokenInterceptor);
        endpoint.publish("/payment");
        return endpoint;
    }

    /** 두 번째 SEI를 같은 Bus에 추가로 publish한다 (멀티 Endpoint). */
    @Bean
    public Endpoint tossPaymentEndpoint(Bus bus,
                                        TossPayServiceImpl tossPayService,
                                        LoggingFeature loggingFeature,
                                        WSS4JInInterceptor usernameTokenInterceptor) {
        EndpointImpl endpoint = new EndpointImpl(bus, tossPayService);
        endpoint.getFeatures().add(loggingFeature);
        endpoint.getInInterceptors().add(usernameTokenInterceptor);
        endpoint.publish("/toss-payment");
        return endpoint;
    }

    /**
     * Contract First Endpoint.
     * Java First와 다른 두 가지를 여기서 확인할 수 있다.
     *   - setWsdlLocation: 생성된 WSDL이 아니라 손으로 쓴 WSDL 원본을 그대로 publish한다
     *   - schema-validation-enabled: 계약 위반 요청을 비즈니스 코드에 닿기 전에 Fault로 막는다
     */
    @Bean
    public Endpoint contractFirstCardEndpoint(Bus bus,
                                              CardPaymentEndpoint cardPaymentEndpoint,
                                              LoggingFeature loggingFeature,
                                              WSS4JInInterceptor usernameTokenInterceptor) {
        EndpointImpl endpoint = new EndpointImpl(bus, cardPaymentEndpoint);
        endpoint.setWsdlLocation("classpath:wsdl/card-payment-v1.wsdl");
        endpoint.setProperties(new HashMap<>(
                Map.of("schema-validation-enabled", Boolean.TRUE)));
        endpoint.getFeatures().add(loggingFeature);
        endpoint.getInInterceptors().add(usernameTokenInterceptor);
        endpoint.publish("/card-payment-v1");
        return endpoint;
    }
}
