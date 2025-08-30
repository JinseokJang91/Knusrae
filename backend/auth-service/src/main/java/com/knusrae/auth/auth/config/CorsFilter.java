package com.knusrae.auth.auth.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // CORS 헤더 설정
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // (GOOGLE) Cross-Origin 정책 헤더 설정
        final String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/auth/google") || path.startsWith("/oauth2/callback/google")
                || path.startsWith("/login/oauth2/code/")) {
            httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
            httpResponse.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
        }

        // Referrer 정책 설정
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // OPTIONS 요청 처리
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}