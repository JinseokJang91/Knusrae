package com.knusrae.auth.api.domain.service;

import com.knusrae.auth.api.domain.entity.RefreshToken;
import com.knusrae.auth.api.domain.repository.RefreshTokenRepository;
import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.security.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 소셜 로그인 사용자를 위한 Access Token과 Refresh Token을 발행합니다.
     * 기존 Refresh Token이 있다면 삭제하고 새로운 토큰을 발행합니다.
     * 
     * @param userId 사용자 ID
     * @param username 사용자 이름
     * @param role 사용자 역할 (SocialRole)
     * @return TokenResponse (Access Token, Refresh Token, 만료 시간 포함)
     */
    @Transactional
    public TokenResponse loginWithSocialUser(Long userId, String username, String role) {
        // 기존 Refresh Token이 있으면 삭제 (토큰 Rotation 정책)
        refreshTokenRepository.findByUserId(userId).ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            log.debug("기존 Refresh Token 삭제: userId={}", userId);
        });

        // Access Token 생성
        String accessToken = tokenProvider.createAccessToken(
                String.valueOf(userId),
                Map.of("role", role, "username", username)
        );

        // Refresh Token 생성
        String refreshToken = tokenProvider.createRefreshToken(String.valueOf(userId));

        // Refresh Token 만료 시간 계산 (현재 시간 + TTL)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshTokenTtl());

        // Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .userId(userId)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.debug("토큰 발행 완료: userId={}, accessTokenExpiresIn={}초, refreshTokenExpiresIn={}초",
                userId, tokenProvider.getAccessTokenTtl(), tokenProvider.getRefreshTokenTtl());

        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenProvider.getAccessTokenTtl(),
                tokenProvider.getRefreshTokenTtl()
        );
    }
}
