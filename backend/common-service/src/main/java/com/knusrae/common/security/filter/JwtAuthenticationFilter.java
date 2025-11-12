package com.knusrae.common.security.filter;

import com.knusrae.common.security.provider.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
<<<<<<< Updated upstream
=======
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // TokenBlacklistRepository는 auth-service에 있으므로 선택적 주입
    // auth-service가 아닌 다른 서비스에서는 null이 될 수 있음
    // 리플렉션을 사용하여 동적으로 호출
    private final org.springframework.beans.factory.BeanFactory beanFactory;
>>>>>>> Stashed changes

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            
            // 1. 블랙리스트 확인 (auth-service에서만 동작)
            if (isTokenBlacklisted(token)) {
                handleJwtException(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_BLACKLISTED", "로그아웃된 토큰입니다.");
                return;
            }
            
            try {
                Claims claims = tokenProvider.parse(token).getBody();
                String subject = claims.getSubject();
                Object role = claims.get("role");
                var auth = new UsernamePasswordAuthenticationToken(
                        subject, null,
                        role == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {}
        }
        filterChain.doFilter(request, response);
    }
<<<<<<< Updated upstream
=======

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
>>>>>>> Stashed changes
}
