package com.knusrae.auth.api.web.response;

/**
 * JWT 토큰 응답 DTO
 * 
 * @param accessToken Access Token
 * @param refreshToken Refresh Token (Phase 2에서 사용 예정, 현재는 null)
 * @param accessTokenExpiresIn Access Token 만료 시간 (초 단위)
 * @param refreshTokenExpiresIn Refresh Token 만료 시간 (초 단위, Phase 2에서 사용 예정, 현재는 0)
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    /**
     * 기존 코드와의 호환성을 위한 생성자
     * refreshToken은 null, expiresIn은 0으로 설정
     */
    public TokenResponse(String accessToken) {
        this(accessToken, null, 0L, 0L);
    }
}
