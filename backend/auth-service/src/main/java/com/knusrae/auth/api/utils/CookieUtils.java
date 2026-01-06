package com.knusrae.auth.api.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 쿠키 생성 유틸리티 클래스
 */
@Component
public class CookieUtils {
    
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;
    
    /**
     * Access Token 쿠키를 생성합니다.
     * 
     * @param token Access Token
     * @param maxAgeSeconds 만료 시간 (초)
     * @return ResponseCookie
     */
    public ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite("Lax")
                .build();
    }
    
    /**
     * Refresh Token 쿠키를 생성합니다.
     * 
     * @param token Refresh Token
     * @param maxAgeSeconds 만료 시간 (초)
     * @return ResponseCookie
     */
    public ResponseCookie createRefreshTokenCookie(String token, long maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite("Lax")
                .build();
    }
    
    /**
     * Access Token 쿠키를 삭제하기 위한 빈 쿠키를 생성합니다.
     * 
     * @return ResponseCookie
     */
    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
    
    /**
     * Refresh Token 쿠키를 삭제하기 위한 빈 쿠키를 생성합니다.
     * 
     * @return ResponseCookie
     */
    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}

