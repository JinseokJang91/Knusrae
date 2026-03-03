package com.knusrae.common.security;

/**
 * Access Token 블랙리스트(로그아웃된 토큰) 조회용 인터페이스.
 * auth-service에서 구현하여 주입하며, member/cook 서비스에서는 빈이 없을 수 있음.
 */
public interface TokenBlacklistChecker {

    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인합니다.
     * @param token Access Token 문자열
     * @return 블랙리스트에 있으면 true
     */
    boolean isBlacklisted(String token);
}
