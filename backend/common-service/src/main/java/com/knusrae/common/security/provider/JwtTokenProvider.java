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

    public Jws<Claims> parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(accessTokenKey)
                .build()
                .parseClaimsJws(token);
    }

    public Jws<Claims> parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(refreshTokenKey)
                .build()
                .parseClaimsJws(token);
    }

}
