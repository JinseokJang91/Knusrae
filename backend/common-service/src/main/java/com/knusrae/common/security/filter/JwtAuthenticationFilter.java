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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final org.springframework.beans.factory.BeanFactory beanFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // /api/auth/refresh 엔드포인트는 accessToken 검증을 건너뜀 (refreshToken만 필요)
        String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.equals("/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookie(request);

        if (StringUtils.hasText(token)) {
            if (isTokenBlacklisted(token)) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_BLACKLISTED", "로그아웃된 토큰입니다.");
                return;
            }
            
            try {
                Claims claims = tokenProvider.parseAccessToken(token).getBody();
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

    private String extractTokenFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    log.info("accessToken Name : {}", cookie.getName());
                    log.info("accessToken Value : {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isTokenBlacklisted(String token) {
        try {
            Object repository = beanFactory.getBean("tokenBlacklistRepository");
            if (!ObjectUtils.isEmpty(repository)) {
                Method method = repository.getClass().getMethod("findByToken", String.class);
                Object result = method.invoke(repository, token);
                if (result instanceof Optional) {
                    return ((Optional<?>) result).isPresent();
                }
            }
        } catch (Exception e) {
            log.debug("토큰 블랙리스트 확인 중 오류 발생 (무시됨): {}", e.getMessage());
        }
        return false;
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
