package com.knusrae.auth.api.web;

import com.knusrae.auth.api.domain.service.GoogleAuthService;
import com.knusrae.auth.api.domain.service.KakaoAuthService;
import com.knusrae.auth.api.domain.service.NaverAuthService;
import com.knusrae.auth.api.domain.service.TokenService;
import com.knusrae.auth.api.web.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final NaverAuthService naverAuthService;
    private final GoogleAuthService googleAuthService;
    private final KakaoAuthService kakaoAuthService;

    private static final String API_BASE_URL = "http://localhost:5173";

    private static final String NAVER_REDIRECT_URI = "/auth/naver/callback";
    private static final String GOOGLE_REDIRECT_URI = "/auth/google/callback";
    private static final String KAKAO_REDIRECT_URI = "/auth/kakao/callback";
    private final TokenService tokenService;

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            TokenResponse tokenResponse = naverAuthService.naverLoginProcess(code, state);
            
            String accessToken = tokenResponse.accessToken();
            
            String redirectUrl = String.format(
                    API_BASE_URL + NAVER_REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(API_BASE_URL + NAVER_REDIRECT_URI);
        }
    }

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = googleAuthService.googleLoginProcess(code);

            String accessToken = tokenResponse.accessToken();

            String redirectUrl = String.format(
                    API_BASE_URL + GOOGLE_REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );

            // GOOGLE은 COOP, COEP header 설정 추가
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(API_BASE_URL + GOOGLE_REDIRECT_URI);
        }
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = kakaoAuthService.kakaoLoginProcess(code);

            String accessToken = tokenResponse.accessToken();

            String redirectUrl = String.format(
                    API_BASE_URL + KAKAO_REDIRECT_URI + "?success=true&accessToken=%s",
                    java.net.URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
            );

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(API_BASE_URL + KAKAO_REDIRECT_URI);
        }
    }

    private ResponseEntity<String> getErrorRedirectResponse(String baseRedirectUri) {
        try {
            String redirectUrl = baseRedirectUri + "?success=false&error=" +
                    java.net.URLEncoder.encode("로그인 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TEST
    @GetMapping("/jwt/token")
    public ResponseEntity<String> getJwtToken(@RequestParam("user_id") Long userId, @RequestParam("user_name") String userName) {
        try {
            TokenResponse tokenResponse = tokenService.loginWithSocialUser(userId, userName, "GOOGLE");

            String accessToken = tokenResponse.accessToken();

            return ResponseEntity.ok().body(accessToken);
        } catch (Exception e) {
            log.error("Jwt Token Error", e);
            return ResponseEntity.badRequest().body("Jwt Token Error");
        }
    }
}