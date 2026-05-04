package com.knusrae.common.utils;

import org.springframework.security.core.Authentication;

/**
 * 인증 관련 유틸리티 클래스
 */
public class AuthenticationUtils {
    
    /**
     * Authentication에서 회원 ID를 추출합니다.
     * 
     * @param authentication Spring Security Authentication 객체
     * @return 회원 ID
     * @throws org.springframework.security.authentication.BadCredentialsException 인증 정보가 없는 경우
     * @throws IllegalArgumentException 유효하지 않은 회원 ID 형식인 경우
     */
    public static Long extractMemberId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.security.authentication.BadCredentialsException("인증 정보가 없습니다.");
        }
        
        try {
            String memberIdStr = authentication.getPrincipal().toString();
            return Long.parseLong(memberIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("유효하지 않은 회원 ID 형식입니다.");
        }
    }

    /**
     * Authentication에서 회원 ID를 추출합니다. 비로그인(익명) 요청이면 null을 반환합니다.
     *
     * @param authentication Spring Security Authentication 객체 (null 허용)
     * @return 회원 ID, 비로그인이면 null
     */
    public static Long extractMemberIdOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

