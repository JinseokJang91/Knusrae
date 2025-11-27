package com.knusrae.auth.api.web;

import com.knusrae.auth.api.domain.service.GoogleAuthService;
import com.knusrae.auth.api.domain.service.KakaoAuthService;
import com.knusrae.auth.api.domain.service.NaverAuthService;
import com.knusrae.auth.api.domain.service.TokenService;
import com.knusrae.auth.api.web.request.TestLoginRequest;
import com.knusrae.auth.api.web.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final TokenService tokenService;

    @Value("${app.test-login.enabled:false}")
    private boolean testLoginEnabled;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final String NAVER_REDIRECT_URI = "/auth/naver/callback";
    private static final String GOOGLE_REDIRECT_URI = "/auth/google/callback";
    private static final String KAKAO_REDIRECT_URI = "/auth/kakao/callback";

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            TokenResponse tokenResponse = naverAuthService.naverLoginProcess(code, state);

            String redirectUrl = frontendUrl + NAVER_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + NAVER_REDIRECT_URI);
        }
    }

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = googleAuthService.googleLoginProcess(code);

            String redirectUrl = frontendUrl + GOOGLE_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse)
                    .header("Cross-Origin-Opener-Policy", "same-origin-allow-popups")
                    .header("Cross-Origin-Embedder-Policy", "unsafe-none")
                    .build();
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + GOOGLE_REDIRECT_URI);
        }
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        try {
            TokenResponse tokenResponse = kakaoAuthService.kakaoLoginProcess(code);

            String redirectUrl = frontendUrl + KAKAO_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse).build();
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + KAKAO_REDIRECT_URI);
        }
    }

    private ResponseEntity.HeadersBuilder<?> buildSuccessResponse(String redirectUrl, TokenResponse tokenResponse) {
        // Access Token 쿠키 설정 (HttpOnly, Secure, SameSite)
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                .httpOnly(true)
                .secure(false) // TODO DEV - false / PROD - true
                .path("/")
                .maxAge(Duration.ofSeconds(tokenResponse.accessTokenExpiresIn()))
                .sameSite("Lax")
                .build();

        // Refresh Token 쿠키 설정 (HttpOnly, Secure, SameSite)
        ResponseCookie refreshTokenCookie = null;
        if (tokenResponse.refreshToken() != null) {
            refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.refreshTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();
        }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            tokenService.logout(accessToken, refreshToken);
            
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();
            
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
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

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            if (StringUtils.isBlank(refreshToken) || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "REFRESH_TOKEN_REQUIRED", "message", "Refresh Token이 필요합니다."));
            }

            TokenResponse tokenResponse = tokenService.refreshAccessToken(refreshToken);

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.accessTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
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

    // TODO 토큰 발급 TEST EndPoint - 개발용
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

    @PostMapping("/test/login")
    public ResponseEntity<?> testLogin(@Valid @RequestBody TestLoginRequest request) {
        if (!testLoginEnabled) {
            log.warn("테스트 로그인 시도가 차단되었습니다. (비활성화됨)");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", "테스트 로그인이 비활성화되었습니다."));
        }
        
        try {
            TokenResponse tokenResponse = tokenService.loginWithTestAccount(request.getEmail());

            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.accessToken())
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.accessTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.refreshToken())
                    .httpOnly(true)
                    .secure(false) // TODO DEV - false / PROD - true
                    .path("/")
                    .maxAge(Duration.ofSeconds(tokenResponse.refreshTokenExpiresIn()))
                    .sameSite("Lax")
                    .build();

            log.info("테스트 로그인 성공: {}", request.getEmail());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(Map.of(
                            "message", "테스트 로그인 성공",
                            "email", request.getEmail(),
                            "accessTokenExpiresIn", tokenResponse.accessTokenExpiresIn(),
                            "refreshTokenExpiresIn", tokenResponse.refreshTokenExpiresIn()
                    ));
        } catch (IllegalArgumentException e) {
            log.warn("테스트 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "USER_NOT_FOUND", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("테스트 로그인 처리 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/test/accounts")
    public ResponseEntity<?> getTestAccounts() {
        if (!testLoginEnabled) {
            log.warn("테스트 계정 조회 시도가 차단되었습니다. (비활성화됨)");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", "테스트 로그인이 비활성화되었습니다."));
        }
        
        try {
            return ResponseEntity.ok(tokenService.getTestAccounts());
        } catch (Exception e) {
            log.error("테스트 계정 조회 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "테스트 계정 조회 중 오류가 발생했습니다."));
        }
    }
}