package com.poc.payment.config;

import com.poc.payment.webservice.card.PaymentServiceImpl;
import com.poc.payment.webservice.toss.TossPayServiceImpl;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SOAP Endpoint 등록.
 * cxf.path(application.yml) = /services 이므로 최종 주소는
 *   카드 Endpoint : http://localhost:8080/services/payment      (?wsdl)
 *   토스 Endpoint : http://localhost:8080/services/toss-payment (?wsdl)
 *
 * 하나의 Bus에 SEI마다 Endpoint를 따로 publish한다.
 * 네임스페이스가 서로 달라야 WSDL/JAXB 컨텍스트가 충돌하지 않는다.
 */
@Configuration
public class CxfConfig {

    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature feature = new LoggingFeature();
        feature.setPrettyLogging(true);
        return feature;
    }

    @Bean
    public Endpoint paymentEndpoint(Bus bus,
                                    PaymentServiceImpl paymentService,
                                    LoggingFeature loggingFeature) {
        EndpointImpl endpoint = new EndpointImpl(bus, paymentService);
        endpoint.getFeatures().add(loggingFeature);
        endpoint.publish("/payment");
        return endpoint;
    }

    /** 두 번째 SEI를 같은 Bus에 추가로 publish한다 (멀티 Endpoint). */
    @Bean
    public Endpoint tossPaymentEndpoint(Bus bus,
                                        TossPayServiceImpl tossPayService,
                                        LoggingFeature loggingFeature) {
        EndpointImpl endpoint = new EndpointImpl(bus, tossPayService);
        endpoint.getFeatures().add(loggingFeature);
        endpoint.publish("/toss-payment");
        return endpoint;
    }
}
