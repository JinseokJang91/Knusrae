package com.knusrae.auth.auth.service;

import com.knusrae.auth.auth.service.response.TokenResponse;
import com.knusrae.common.security.JwtTokenProvider;
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
        return new TokenResponse(token);
    }
}
