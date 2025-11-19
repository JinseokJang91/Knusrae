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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {
    private final NaverAuthService naverAuthService;
    private final GoogleAuthService googleAuthService;
    private final KakaoAuthService kakaoAuthService;

    private static final String API_BASE_URL = "http://localhost:5173"; // TODO 로컬 삭제

    private static final String NAVER_REDIRECT_URI = "/auth/naver/callback";
    private static final String GOOGLE_REDIRECT_URI = "/auth/google/callback";
    private static final String KAKAO_REDIRECT_URI = "/auth/kakao/callback";
    private final TokenService tokenService;

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            TokenResponse tokenResponse = naverAuthService.naverLoginProcess(code, state);

            String redirectUrl = API_BASE_URL + NAVER_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
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

            String redirectUrl = API_BASE_URL + GOOGLE_REDIRECT_URI + "?success=true";

            // GOOGLE은 COOP, COEP header 설정 추가
            return buildSuccessResponse(redirectUrl, tokenResponse)
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

            String redirectUrl = API_BASE_URL + KAKAO_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse).build();
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(API_BASE_URL + KAKAO_REDIRECT_URI);
        }
    }

    /**
     * 로그인 성공 시 쿠키에 토큰을 설정하고 리다이렉트합니다.
     * 보안을 위해 쿠키에만 토큰을 저장하고 URL에는 포함하지 않습니다.
     * 
     * @param redirectUrl 리다이렉트 URL
     * @param tokenResponse 토큰 응답 (Access Token, Refresh Token 포함)
     * @return ResponseEntity builder
     */
    private ResponseEntity.HeadersBuilder<?> buildSuccessResponse(String redirectUrl, TokenResponse tokenResponse) {
        // Access Token 쿠키 설정 (HttpOnly, Secure, SameSite)
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                .httpOnly(true)
                .secure(false) // 개발 환경에서는 false, 프로덕션에서는 true
                .path("/")
                .maxAge(Duration.ofSeconds(tokenResponse.accessTokenExpiresIn()))
                .sameSite("Lax")
                .build();

        // Refresh Token 쿠키 설정 (HttpOnly, Secure, SameSite)
        ResponseCookie refreshTokenCookie = null;
        if (tokenResponse.refreshToken() != null) {
            refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발 환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.refreshTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();
        }

        // URL에는 토큰을 포함하지 않음 (보안 강화)
        ResponseEntity.HeadersBuilder<?> responseBuilder = ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        if (refreshTokenCookie != null) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        return responseBuilder;
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

    /**
     * 로그아웃 처리: Access Token과 Refresh Token을 무효화하고 쿠키를 삭제합니다.
     * 
     * @param accessToken 쿠키에서 받은 Access Token
     * @param refreshToken 쿠키에서 받은 Refresh Token
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            // 로그아웃 처리
            tokenService.logout(accessToken, refreshToken);
            
            // 쿠키 삭제
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(Map.of("message", "로그아웃되었습니다."));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발행합니다.
     * 
     * @param refreshToken 쿠키에서 받은 Refresh Token
     * @return TokenResponse (새 Access Token, 새 Refresh Token, 만료 시간 포함)
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            // 쿠키에서 Refresh Token 추출
            String refreshTokenValue = refreshToken;
            
            if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "REFRESH_TOKEN_REQUIRED", "message", "Refresh Token이 필요합니다."));
            }

            // 토큰 갱신
            TokenResponse tokenResponse = tokenService.refreshAccessToken(refreshTokenValue);

            // 새 토큰을 쿠키에 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                    .httpOnly(true)
                    .secure(false) // 개발 환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.accessTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발 환경에서는 false, 프로덕션에서는 true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.refreshTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(tokenResponse);
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "INVALID_REFRESH_TOKEN", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "토큰 갱신 중 오류가 발생했습니다."));
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