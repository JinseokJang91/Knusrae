package com.knusrae.auth.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.service.TokenService;
import com.knusrae.auth.auth.service.NaverAuthService;
import com.knusrae.auth.auth.service.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final NaverAuthService naverAuthService;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    private static final String REDIRECT_URI = "http://localhost:5173/auth/naver/callback";

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            log.info("Naver OAuth callback received. code={}, state={}", code, state);
            TokenResponse tokenResponse = naverAuthService.naverLoginProcess(code, state);
            System.out.println("tokenResponse = " + tokenResponse);
            String accessToken = tokenResponse.accessToken();
            log.info("Extracted access token: {}", accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null");
            String redirectUrl = String.format(
                    REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );
            log.info("Redirecting to: {}", redirectUrl);

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

            log.info("Error redirecting to: {}", redirectUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}