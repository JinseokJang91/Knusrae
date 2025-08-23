package com.knusrae.auth.auth.service;

import com.knusrae.auth.auth.web.request.LoginRequest;
import com.knusrae.auth.auth.web.response.TokenResponse;
import com.knusrae.common.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationConfiguration config, JwtTokenProvider tokenProvider) throws Exception {
        this.authManager = config.getAuthenticationManager();
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        String role = auth.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse("USER");
        String token = tokenProvider.createToken(auth.getName(), Map.of("role", role));

        return new TokenResponse(token);
    }
}
