package com.knusrae.common.security.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/auth/google") || path.startsWith("/oauth2/callback/google")
                || path.startsWith("/login/oauth2/code/")) {
            httpResponse.setHeader("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
            httpResponse.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
        }

        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}