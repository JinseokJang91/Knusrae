package com.knusrae.auth.api.domain.service;

import com.knusrae.auth.api.domain.entity.RefreshToken;
import com.knusrae.auth.api.domain.entity.TokenBlacklist;
import com.knusrae.auth.api.domain.repository.RefreshTokenRepository;
import com.knusrae.auth.api.domain.repository.TokenBlacklistRepository;
import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.security.provider.JwtTokenProvider;
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
        // 1. 기존 Refresh Token이 있으면 삭제 (토큰 Rotation 정책)
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(existingToken -> {
            refreshTokenRepository.delete(existingToken);
            log.debug("기존 Refresh Token 삭제: userId={}", userId);
        });

        // 2. Access Token 생성
        String accessToken = tokenProvider.createAccessToken(
                String.valueOf(userId),
                Map.of("role", role, "username", username) // TODO Claim 추가
        );

        // 3. Refresh Token 생성
        String refreshToken = tokenProvider.createRefreshToken(String.valueOf(userId)); // TODO Claim 추가

        // Refresh Token 만료 시간 계산 (현재 시간 + TTL)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getRefreshTokenTtl());

        // 4. Refresh Token 저장
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

    /**
     * Refresh Token을 사용하여 새로운 Access Token을 발행합니다.
     * Refresh Token Rotation 정책을 적용하여 기존 Refresh Token을 삭제하고 새로 발행합니다.
     * 
     * @param refreshTokenString Refresh Token 문자열
     * @return TokenResponse (새 Access Token, 새 Refresh Token, 만료 시간 포함)
     * @throws IllegalArgumentException Refresh Token이 유효하지 않거나 만료된 경우
     * @throws io.jsonwebtoken.JwtException Refresh Token 파싱 실패 시
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshTokenString) {
        // 1. Refresh Token 파싱 및 검증
        Jws<Claims> claimsJws = tokenProvider.parseRefreshToken(refreshTokenString);
        Claims claims = claimsJws.getBody();
        
        // 2. tokenType 확인 (REFRESH 토큰인지 확인)
        String tokenType = claims.get("tokenType", String.class);
        if (!"REFRESH".equals(tokenType)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }
        
        // 3. userId 추출
        String userIdStr = claims.getSubject();
        if (userIdStr == null) {
            throw new IllegalArgumentException("Refresh Token에 사용자 ID가 없습니다.");
        }
        Long userId = Long.parseLong(userIdStr);
        
        // 4. DB에서 Refresh Token 조회 및 검증
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Refresh Token입니다."));
        
        // 5. Refresh Token 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 Refresh Token입니다.");
        }
        
        // 6. Member 정보 조회 (role, username 필요)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        // 7. 기존 Refresh Token 삭제 (토큰 Rotation 정책)
        refreshTokenRepository.delete(refreshToken);
        log.debug("기존 Refresh Token 삭제 (Rotation): userId={}", userId);
        
        // 8. 새 Access Token 생성
        String newAccessToken = tokenProvider.createAccessToken(
                String.valueOf(userId),
                Map.of("role", member.getSocialRole().name(), "username", member.getName())
        );
        
        // 9. 새 Refresh Token 생성
        String newRefreshToken = tokenProvider.createRefreshToken(String.valueOf(userId));
        
        // 10. 새 Refresh Token 저장
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

    /**
     * 로그아웃 처리: Access Token과 Refresh Token을 무효화합니다.
     * 
     * @param accessToken Access Token
     * @param refreshToken Refresh Token (선택)
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        try {
            // 1. Access Token을 블랙리스트에 추가
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
                    // 토큰 파싱 실패 시에도 블랙리스트에 추가 (만료된 토큰도 차단)
                    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(tokenProvider.getAccessTokenTtl());
                    TokenBlacklist blacklist = TokenBlacklist.builder()
                            .token(accessToken)
                            .expiresAt(expiresAt)
                            .build();
                    tokenBlacklistRepository.save(blacklist);
                    log.debug("Access Token 블랙리스트 추가 완료 (파싱 실패, 기본 만료 시간 사용)");
                }
            }
            
            // 2. Refresh Token 삭제
            if (!StringUtils.isBlank(refreshToken) && !refreshToken.isBlank()) {
                refreshTokenRepository.findByToken(refreshToken).ifPresent(refreshTokenRepository::delete);
                log.debug("Refresh Token 삭제 완료");
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 테스트 계정으로 로그인 처리
     * 개발/테스트 환경에서만 사용
     * 
     * @param email 테스트 계정 이메일
     * @return TokenResponse (Access Token, Refresh Token, 만료 시간 포함)
     * @throws IllegalArgumentException 존재하지 않는 이메일인 경우
     */
    @Transactional
    public TokenResponse loginWithTestAccount(String email) {
        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(email);
        
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 테스트 계정입니다: " + email);
        }
        
        // 소셜 로그인과 동일한 방식으로 토큰 발급
        return loginWithSocialUser(member.getId(), member.getName(), member.getSocialRole().name());
    }

    /**
     * 사용 가능한 테스트 계정 목록 조회
     * 이메일이 'test'로 시작하는 계정들을 반환
     * 개발/테스트 환경에서만 사용
     * 
     * @return 테스트 계정 목록 (id, name, nickname, email, socialRole)
     */
    public List<Map<String, Object>> getTestAccounts() {
        // test@로 시작하는 이메일을 가진 모든 사용자 조회
        List<Member> testMembers = memberRepository.findAll().stream()
                .filter(member -> member.getEmail() != null && member.getEmail().startsWith("test"))
                .collect(Collectors.toList());
        
        // DTO 변환
        return testMembers.stream()
                .map(member -> {
                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("id", member.getId());
                    accountInfo.put("name", member.getName());
                    accountInfo.put("nickname", member.getNickname());
                    accountInfo.put("email", member.getEmail());
                    accountInfo.put("socialRole", member.getSocialRole().name());
                    accountInfo.put("isActive", member.getIsActive().name());
                    return accountInfo;
                })
                .collect(Collectors.toList());
    }
}
