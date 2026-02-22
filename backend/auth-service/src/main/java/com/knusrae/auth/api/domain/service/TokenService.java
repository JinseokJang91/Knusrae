package com.knusrae.auth.api.domain.service;

import com.knusrae.auth.api.domain.entity.RefreshToken;
import com.knusrae.auth.api.domain.entity.TokenBlacklist;
import com.knusrae.auth.api.domain.repository.RefreshTokenRepository;
import com.knusrae.auth.api.domain.repository.TokenBlacklistRepository;
import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.security.provider.JwtTokenProvider;
import com.knusrae.common.utils.PiiMaskUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TokenResponse loginWithSocialUser(Long userId, String username, String role) {
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            log.debug("기존 Refresh Token 삭제: userId={}", userId);
        });

        String accessToken = tokenProvider.createAccessToken(
                String.valueOf(userId),
                Map.of("role", role, "username", username) // TODO Claim 추가
        );

        String refreshToken = tokenProvider.createRefreshToken(String.valueOf(userId));

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshTokenTtl());

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

    @Transactional
    public TokenResponse refreshAccessToken(String refreshTokenString) {
        Jws<Claims> claimsJws = tokenProvider.parseRefreshToken(refreshTokenString);
        Claims claims = claimsJws.getBody();
        
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        
        String userIdStr = claims.getSubject();
        if (userIdStr == null) {
            throw new IllegalArgumentException("Refresh Token에 사용자 ID가 없습니다.");
        }
        Long userId = Long.parseLong(userIdStr);
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Refresh Token입니다."));
        
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }
        
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        refreshTokenRepository.delete(refreshToken);
        log.debug("기존 Refresh Token 삭제 (Rotation): userId={}", userId);
        
        String newAccessToken = tokenProvider.createAccessToken(
                String.valueOf(userId),
                Map.of("role", member.getSocialRole().name(), "username", member.getName())
        );
        
        String newRefreshToken = tokenProvider.createRefreshToken(String.valueOf(userId));
        
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshTokenTtl());
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .token(newRefreshToken)
                .userId(userId)
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);
        
        log.debug("토큰 갱신 완료: userId={}, accessTokenExpiresIn={}초, refreshTokenExpiresIn={}초",
                userId, tokenProvider.getAccessTokenTtl(), tokenProvider.getRefreshTokenTtl());
        
        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                tokenProvider.getAccessTokenTtl(),
                tokenProvider.getRefreshTokenTtl()
        );
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        try {
            if (accessToken != null && !accessToken.isBlank()) {
                try {
                    Jws<Claims> claimsJws = tokenProvider.parseAccessToken(accessToken);
                    Claims claims = claimsJws.getBody();
                    LocalDateTime expiresAt = LocalDateTime.ofInstant(
                            claims.getExpiration().toInstant(),
                            java.time.ZoneId.systemDefault()
                    );
                    
                    TokenBlacklist blacklist = TokenBlacklist.builder()
                            .token(accessToken)
                            .expiresAt(expiresAt)
                            .build();
                    tokenBlacklistRepository.save(blacklist);
                    log.debug("Access Token 블랙리스트 추가 완료");
                } catch (Exception e) {
                    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getAccessTokenTtl());
                    TokenBlacklist blacklist = TokenBlacklist.builder()
                            .token(accessToken)
                            .expiresAt(expiresAt)
                            .build();
                    tokenBlacklistRepository.save(blacklist);
                    log.debug("Access Token 블랙리스트 추가 완료 (파싱 실패, 기본 만료 시간 사용)");
                }
            }
            
            if (!StringUtils.isBlank(refreshToken)) {
                refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
                log.debug("Refresh Token 삭제 완료");
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional
    public TokenResponse loginWithTestAccount(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테스트 계정입니다."));
        if (member.getEmail() == null || !member.getEmail().startsWith("test")) {
            throw new IllegalArgumentException("테스트 계정이 아닙니다.");
        }
        return loginWithSocialUser(member.getId(), member.getName(), member.getSocialRole().name());
    }

    // TODO 테스트 계정 로그인
    public List<Map<String, Object>> getTestAccounts() {
        List<Member> testMembers = memberRepository.findAll().stream()
                .filter(member -> member.getEmail() != null && member.getEmail().startsWith("test"))
                .collect(Collectors.toList());
        
        return testMembers.stream()
                .map(member -> {
                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("id", member.getId());
                    accountInfo.put("name", member.getName());
                    accountInfo.put("nickname", member.getNickname());
                    accountInfo.put("email", PiiMaskUtils.maskEmail(member.getEmail()));
                    accountInfo.put("socialRole", member.getSocialRole().name());
                    accountInfo.put("isActive", member.getIsActive().name());
                    return accountInfo;
                })
                .collect(Collectors.toList());
    }
}
