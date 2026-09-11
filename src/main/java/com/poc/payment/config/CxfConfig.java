package com.poc.payment.config;

import com.poc.payment.webservice.card.PaymentServiceImpl;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SOAP Endpoint 등록.
 * cxf.path(application.yml) = /services 이므로 최종 주소는
 *   Endpoint : http://localhost:8080/services/payment
 *   WSDL     : http://localhost:8080/services/payment?wsdl
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
}
