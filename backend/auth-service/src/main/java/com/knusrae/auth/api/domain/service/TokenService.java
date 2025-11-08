package com.knusrae.auth.api.domain.service;

import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.security.provider.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TokenService {
    private final JwtTokenProvider tokenProvider;

    public TokenService(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    // Added method for social login
    public TokenResponse loginWithSocialUser(Long userId, String username, String role) {
        String token = tokenProvider.createToken(String.valueOf(userId), Map.of("role", role, "username", username));
        // Phase 1: refreshToken은 null, refreshTokenExpiresIn은 0으로 설정
        // Phase 2에서 실제 Refresh Token 구현 예정
        return new TokenResponse(token, null, tokenProvider.getTtl(), 0L);
    }
}
