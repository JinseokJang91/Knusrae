package com.knusrae.auth.auth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.service.TokenService;
import com.knusrae.auth.auth.service.NaverAuthService;
import com.knusrae.auth.auth.service.GoogleAuthService;
import com.knusrae.auth.auth.service.KakaoAuthService;
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
    private final GoogleAuthService googleAuthService;
    private final KakaoAuthService kakaoAuthService;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    private static final String REDIRECT_URI = "http://localhost:5173/auth/naver/callback";
    private static final String GOOGLE_REDIRECT_URI = "http://localhost:5173/auth/google/callback";
    private static final String KAKAO_REDIRECT_URI = "http://localhost:5173/auth/kakao/callback";

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
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(REDIRECT_URI);
        }
    }

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code) {
        try {
            log.info("Google OAuth callback received. code={}", code);
            TokenResponse tokenResponse = googleAuthService.googleLoginProcess(code);
            System.out.println("tokenResponse = " + tokenResponse);
            String accessToken = tokenResponse.accessToken();
            log.info("Extracted access token: {}", accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null");
            String redirectUrl = String.format(
                    GOOGLE_REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );
            log.info("Redirecting to: {}", redirectUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(GOOGLE_REDIRECT_URI);
        }
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        try {
            log.info("Kakao OAuth callback received. code={}", code);
            TokenResponse tokenResponse = kakaoAuthService.kakaoLoginProcess(code);
            System.out.println("tokenResponse = " + tokenResponse);
            String accessToken = tokenResponse.accessToken();
            log.info("Extracted access token: {}", accessToken != null ? accessToken.substring(0, Math.min(20, accessToken.length())) + "..." : "null");
            String redirectUrl = String.format(
                    KAKAO_REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );
            log.info("Redirecting to: {}", redirectUrl);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(KAKAO_REDIRECT_URI);
        }
    }

    private ResponseEntity<String> getErrorRedirectResponse() {
        return getErrorRedirectResponse(REDIRECT_URI);
    }

    private ResponseEntity<String> getErrorRedirectResponse(String baseRedirectUri) {
        try {
            String redirectUrl = baseRedirectUri + "?success=false&error=" +
                    java.net.URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);

            log.info("Error redirecting to: {}", redirectUrl);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}