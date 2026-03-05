package com.knusrae.auth.api.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * OAuth 2.0 state 파라미터 생성 및 검증 (CSRF 방지).
 * state는 서버에서 서명하여 발급하고, 콜백 시 서명·만료·프로바이더 일치 여부를 검증한다.
 */
@Service
@Slf4j
public class OAuthStateService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long STATE_VALID_SECONDS = 600L; // 10분

    private final byte[] stateSecretBytes;

    public OAuthStateService(@Value("${app.oauth.state-secret}") String stateSecret) {
        this.stateSecretBytes = stateSecret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 해당 프로바이더용 state 토큰 생성.
     * 형식: base64url(provider:expiryMillis).base64url(hmacBytes)
     */
    public String generateState(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider is required");
        }
        long expiryMillis = System.currentTimeMillis() + STATE_VALID_SECONDS * 1000L;
        String payload = provider + ":" + expiryMillis;
        String payloadB64 = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
        byte[] hmacBytes = computeHmacBytes(payload);
        return payloadB64 + "." + base64UrlEncode(hmacBytes);
    }

    /**
     * state 검증: 서명 일치, 만료 전, 프로바이더 일치.
     *
     * @param expectedProvider 콜백 엔드포인트의 프로바이더(naver, google, kakao)
     * @param state            클라이언트에서 전달된 state
     * @throws InvalidOAuthStateException 검증 실패 시
     */
    public void validateState(String expectedProvider, String state) {
        if (expectedProvider == null || expectedProvider.isBlank()) {
            throw new InvalidOAuthStateException("provider is required");
        }
        if (state == null || state.isBlank()) {
            throw new InvalidOAuthStateException("state is required");
        }

        int dot = state.indexOf('.');
        if (dot <= 0 || dot >= state.length() - 1) {
            throw new InvalidOAuthStateException("invalid state format");
        }

        String payloadB64 = state.substring(0, dot);
        String signatureB64 = state.substring(dot + 1);

        String payload;
        try {
            payload = new String(base64UrlDecode(payloadB64), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.debug("state payload decode failed: {}", e.getMessage());
            throw new InvalidOAuthStateException("invalid state encoding");
        }

        byte[] expectedSignature = computeHmacBytes(payload);
        byte[] receivedSignature;
        try {
            receivedSignature = base64UrlDecode(signatureB64);
        } catch (Exception e) {
            log.debug("state signature decode failed: {}", e.getMessage());
            throw new InvalidOAuthStateException("invalid state encoding");
        }
        if (!constantTimeEquals(expectedSignature, receivedSignature)) {
            throw new InvalidOAuthStateException("state signature mismatch");
        }

        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            throw new InvalidOAuthStateException("invalid state payload");
        }
        String provider = parts[0];
        long expiryMillis;
        try {
            expiryMillis = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new InvalidOAuthStateException("invalid state expiry");
        }

        if (!expectedProvider.equalsIgnoreCase(provider)) {
            throw new InvalidOAuthStateException("state provider mismatch");
        }
        if (System.currentTimeMillis() > expiryMillis) {
            throw new InvalidOAuthStateException("state expired");
        }
    }

    private byte[] computeHmacBytes(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(stateSecretBytes, HMAC_ALGORITHM));
            return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    public static final class InvalidOAuthStateException extends RuntimeException {
        public InvalidOAuthStateException(String message) {
            super(message);
        }
    }
}
