package com.poc.payment.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.stereotype.Component;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * WSS4J가 UsernameToken을 검증할 때 호출한다.
 * 여기서는 "이 사용자의 저장된 비밀번호"만 알려주고,
 * 실제 대조는 WSS4J가 한다. 불일치면 WSS4J가 SOAP Fault를 만든다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UsernameTokenCallbackHandler implements CallbackHandler {

    private final SoapSecurityProperties properties;

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (!(callback instanceof WSPasswordCallback passwordCallback)) {
                throw new UnsupportedCallbackException(callback, "지원하지 않는 Callback입니다");
            }
            if (passwordCallback.getUsage() != WSPasswordCallback.USERNAME_TOKEN) {
                continue;
            }
            String identifier = passwordCallback.getIdentifier();
            if (!properties.username().equals(identifier)) {
                log.warn("[WS-Security] 알 수 없는 사용자: {}", identifier);
                // IOException을 던지면 WSS4J가 인증 실패 Fault로 바꾼다.
                throw new IOException("알 수 없는 사용자입니다: " + identifier);
            }
            passwordCallback.setPassword(properties.password());
        }
    }
}
