package com.knusrae.auth.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.dto.NaverUserDTO;
import com.knusrae.auth.auth.service.NaverAuthService;
import com.knusrae.common.security.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

record LoginRequest(@NotBlank String username, @NotBlank String password) {}
record TokenResponse(String accessToken) {}

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final NaverAuthService naverAuthService;
    private final ObjectMapper objectMapper;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;

    private static final String REDIRECT_URI = "http://localhost:5173/auth/naver/callback";

    public AuthController(NaverAuthService naverAuthService, ObjectMapper objectMapper, AuthenticationConfiguration config, JwtTokenProvider tokenProvider) throws Exception {

        this.naverAuthService = naverAuthService;
        this.objectMapper = objectMapper;
        this.authManager = config.getAuthenticationManager();
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            log.info("Naver OAuth callback received. code={}, state={}", code, state);
            NaverUserDTO userDto = naverAuthService.naverLoginProcess(code, state);
            String userJson = objectMapper.writeValueAsString(userDto);
            String redirectUrl = String.format(
                    REDIRECT_URI + "?success=true&user=%s",
                    java.net.URLEncoder.encode(userJson, StandardCharsets.UTF_8)
            );
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류", e);
            return getErrorRedirectResponse();
        }
    }

    private ResponseEntity<String> getErrorRedirectResponse() {
        try {
            String redirectUrl = REDIRECT_URI + "?success=false&error=" +
                    java.net.URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        String role = auth.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse("USER");
        String token = tokenProvider.createToken(auth.getName(), Map.of("role", role));
        return ResponseEntity.ok(new TokenResponse(token));
    }
}