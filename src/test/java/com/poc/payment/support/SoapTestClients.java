package com.poc.payment.support;

import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.dom.WSConstants;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * 테스트용 SOAP 클라이언트 팩토리.
 * 서버가 wsse:UsernameToken을 요구하므로 클라이언트도 헤더를 붙여야 한다.
 */
public final class SoapTestClients {

    public static final String USERNAME = "poc-client";
    public static final String PASSWORD = "poc-secret";

    private SoapTestClients() {
    }

    /** 인증 헤더 없는 클라이언트 — 인증 실패를 확인할 때 쓴다. */
    public static <T> T plain(Class<T> sei, int port, String path) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(sei);
        factory.setAddress("http://localhost:" + port + path);
        factory.getFeatures().add(new LoggingFeature());
        return factory.create(sei);
    }

    /** 설정된 정상 계정으로 UsernameToken을 붙인 클라이언트. */
    public static <T> T secured(Class<T> sei, int port, String path) {
        return secured(sei, port, path, USERNAME, PASSWORD);
    }

    public static <T> T secured(Class<T> sei, int port, String path,
                                String username, String password) {
        T proxy = plain(sei, port, path);
        ClientProxy.getClient(proxy).getOutInterceptors()
                .add(usernameToken(username, password));
        return proxy;
    }

    private static WSS4JOutInterceptor usernameToken(String username, String password) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN);
        properties.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        properties.put(ConfigurationConstants.USER, username);
        properties.put(ConfigurationConstants.PW_CALLBACK_REF, (CallbackHandler) callbacks -> {
            for (Callback callback : callbacks) {
                ((WSPasswordCallback) callback).setPassword(password);
            }
        });
        return new WSS4JOutInterceptor(properties);
    }
}
