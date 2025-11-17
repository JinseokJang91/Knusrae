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
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // TokenBlacklistRepository는 auth-service에 있으므로 선택적 주입
    // auth-service가 아닌 다른 서비스에서는 null이 될 수 있음
    // 리플렉션을 사용하여 동적으로 호출
    private final org.springframework.beans.factory.BeanFactory beanFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 쿠키에서 토큰 추출
        String token = extractTokenFromCookie(request);
        
        if (StringUtils.hasText(token)) {
            // 1. 블랙리스트 확인 (auth-service에서만 동작)
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

    /**
     * 쿠키에서 토큰을 추출합니다.
     * 
     * @param request HTTP 요청
     * @return 토큰 문자열 (없으면 null)
     */
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

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     * auth-service가 아닌 다른 서비스에서는 항상 false를 반환합니다.
     * 
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            // TokenBlacklistRepository가 존재하는지 확인 (auth-service에서만 존재)
            Object repository = beanFactory.getBean("tokenBlacklistRepository");
            if (repository != null) {
                // 리플렉션을 사용하여 findByToken 메서드 호출
                java.lang.reflect.Method method = repository.getClass().getMethod("findByToken", String.class);
                Object result = method.invoke(repository, token);
                if (result instanceof java.util.Optional) {
                    return ((java.util.Optional<?>) result).isPresent();
                }
            }
        } catch (Exception e) {
            // Bean이 존재하지 않거나 메서드 호출 실패 시 false 반환 (다른 서비스에서는 정상)
            // 로그는 남기지 않음 (다른 서비스에서는 정상적인 상황)
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
