package com.knusrae.common.security.provider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final Key accessTokenKey;
    private final Key refreshTokenKey;

    @Getter
    private final long accessTokenTtl; // Access Token의 TTL(Time To Live)
    @Getter
    private final long refreshTokenTtl; // Refresh Token의 TTL(Time To Live)

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String accessTokenSecret,
            @Value("${spring.security.jwt.accessTokenTtlSeconds}") long accessTokenTtl,
            @Value("${spring.security.jwt.refreshTokenSecret}") String refreshTokenSecret,
            @Value("${spring.security.jwt.refreshTokenTtlSeconds}") long refreshTokenTtl) {
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessTokenSecret);
        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshTokenSecret);
        this.accessTokenKey = Keys.hmacShaKeyFor(accessKeyBytes);
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    /**
     * Access Token을 생성합니다.
     * 
     * @param subject 사용자 ID
     * @param claims 추가 클레임 (role, username 등)
     * @return Access Token
     */
    public String createAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("tokenType", "ACCESS");

        return Jwts.builder()
                .setClaims(tokenClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTokenTtl)))
                .signWith(accessTokenKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token을 생성합니다.
     * 
     * @param subject 사용자 ID
     * @return Refresh Token
     */
    public String createRefreshToken(String subject) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTokenTtl)))
                .signWith(refreshTokenKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Access Token을 파싱합니다.
     * 
     * @param token Access Token
     * @return 파싱된 Claims
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않을 경우
     */
    public Jws<Claims> parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessTokenKey)
                .build()
                .parseClaimsJws(token);
    }

    /**
     * Refresh Token을 파싱합니다.
     * 
     * @param token Refresh Token
     * @return 파싱된 Claims
     * @throws io.jsonwebtoken.JwtException 토큰이 유효하지 않을 경우
     */
    public Jws<Claims> parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(token);
    }

    /**
     * 레거시 호환성을 위한 메서드 (기존 코드와의 호환성 유지)
     * Access Token을 생성합니다.
     * 
     * @deprecated createAccessToken 메서드를 사용하세요.
     */
    @Deprecated
    public String createToken(String subject, Map<String, Object> claims) {
        return createAccessToken(subject, claims);
    }

    /**
     * 레거시 호환성을 위한 메서드 (기존 코드와의 호환성 유지)
     * Access Token을 파싱합니다.
     * 
     * @deprecated parseAccessToken 메서드를 사용하세요.
     */
    @Deprecated
    public Jws<Claims> parse(String token) {
        return parseAccessToken(token);
    }

    /**
     * 레거시 호환성을 위한 메서드
     * @deprecated getAccessTokenTtl 메서드를 사용하세요.
     */
    @Deprecated
    public long getTtl() {
        return accessTokenTtl;
    }
}
