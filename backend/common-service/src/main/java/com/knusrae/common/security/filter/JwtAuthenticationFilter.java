package com.knusrae.common.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.common.security.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = tokenProvider.parse(token).getBody();
                String subject = claims.getSubject();
                Object role = claims.get("role");
                var auth = new UsernamePasswordAuthenticationToken(
                        subject, null,
                        role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
                return;
            } catch (MalformedJwtException e) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰 형식입니다.");
                return;
            } catch (SignatureException e) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_SIGNATURE", "토큰 서명이 유효하지 않습니다.");
                return;
            } catch (IllegalArgumentException e) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN", "토큰이 비어있거나 형식이 올바르지 않습니다.");
                return;
            } catch (JwtException e) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT_ERROR", "JWT 처리 중 오류가 발생했습니다: " + e.getMessage());
                return;
            } catch (Exception e) {
                handleJwtException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "인증 처리 중 오류가 발생했습니다.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleJwtException(HttpServletResponse response, int status, String errorCode, String errorMessage) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of(
                "error", errorCode,
                "message", errorMessage
        ));
    }
}
