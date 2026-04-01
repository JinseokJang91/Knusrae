package com.knusrae.auth.api.web;

import com.knusrae.auth.api.domain.service.GoogleAuthService;
import com.knusrae.auth.api.domain.service.KakaoAuthService;
import com.knusrae.auth.api.domain.service.NaverAuthService;
import com.knusrae.auth.api.domain.service.OAuthStateService;
import com.knusrae.auth.api.domain.service.TokenService;
import com.knusrae.auth.api.domain.service.OAuthStateService.InvalidOAuthStateException;
import com.knusrae.auth.api.utils.CookieUtils;
import com.knusrae.auth.api.web.request.TestLoginRequest;
import com.knusrae.auth.api.web.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
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
    private final CookieUtils cookieUtils;
    private final OAuthStateService oauthStateService;

    @Value("${app.test-login.enabled:false}")
    private boolean testLoginEnabled;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final String NAVER_REDIRECT_URI = "/auth/naver/callback";
    private static final String GOOGLE_REDIRECT_URI = "/auth/google/callback";
    private static final String KAKAO_REDIRECT_URI = "/auth/kakao/callback";

    private static final String STATE_ERROR_MESSAGE = "잘못된 요청입니다. 다시 로그인해 주세요.";

    /**
     * OAuth 로그인 시작 전 state 파라미터 발급 (CSRF 방지).
     * 프론트는 이 state를 프로바이더 인증 URL에 넣고, 콜백 시 서버에서 검증한다.
     */
    @GetMapping("/oauth/state")
    public ResponseEntity<Map<String, String>> getOAuthState(@RequestParam("provider") String provider) {
        if (provider == null || !java.util.Set.of("naver", "google", "kakao").contains(provider.toLowerCase())) {
            return ResponseEntity.badRequest().build();
        }
        String state = oauthStateService.generateState(provider.toLowerCase());
        return ResponseEntity.ok(Map.of("state", state));
    }

    @GetMapping("/naver/callback")
    public ResponseEntity<String> naverCallback(@RequestParam("code") String code,
                                                @RequestParam("state") String state) {
        try {
            oauthStateService.validateState("naver", state);
            TokenResponse tokenResponse = naverAuthService.naverLoginProcess(code, state);
            String redirectUrl = frontendUrl + NAVER_REDIRECT_URI + "?success=true";
            return buildSuccessResponse(redirectUrl, tokenResponse, true, false);
        } catch (InvalidOAuthStateException e) {
            log.warn("네이버 로그인 state 검증 실패: {}", e.getMessage());
            return getErrorRedirectResponse(frontendUrl + NAVER_REDIRECT_URI, STATE_ERROR_MESSAGE);
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + NAVER_REDIRECT_URI);
        }
    }

    @GetMapping("/google/callback")
    public ResponseEntity<String> googleCallback(@RequestParam("code") String code,
                                                @RequestParam(value = "state", required = false) String state) {
        try {
            if (state == null || state.isBlank()) {
                return getErrorRedirectResponse(frontendUrl + GOOGLE_REDIRECT_URI, STATE_ERROR_MESSAGE);
            }
            oauthStateService.validateState("google", state);
            TokenResponse tokenResponse = googleAuthService.googleLoginProcess(code);

            String redirectUrl = frontendUrl + GOOGLE_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse, true, true);
        } catch (InvalidOAuthStateException e) {
            log.warn("구글 로그인 state 검증 실패: {}", e.getMessage());
            return getErrorRedirectResponse(frontendUrl + GOOGLE_REDIRECT_URI, STATE_ERROR_MESSAGE);
        } catch (Exception e) {
            log.error("구글 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + GOOGLE_REDIRECT_URI);
        }
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code,
                                               @RequestParam(value = "state", required = false) String state) {
        try {
            if (state == null || state.isBlank()) {
                return getErrorRedirectResponse(frontendUrl + KAKAO_REDIRECT_URI, STATE_ERROR_MESSAGE);
            }
            oauthStateService.validateState("kakao", state);
            TokenResponse tokenResponse = kakaoAuthService.kakaoLoginProcess(code);

            String redirectUrl = frontendUrl + KAKAO_REDIRECT_URI + "?success=true";

            return buildSuccessResponse(redirectUrl, tokenResponse, false, false);
        } catch (InvalidOAuthStateException e) {
            log.warn("카카오 로그인 state 검증 실패: {}", e.getMessage());
            return getErrorRedirectResponse(frontendUrl + KAKAO_REDIRECT_URI, STATE_ERROR_MESSAGE);
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류", e);
            return getErrorRedirectResponse(frontendUrl + KAKAO_REDIRECT_URI);
        }
    }

    private ResponseEntity<String> buildSuccessResponse(String redirectUrl, TokenResponse tokenResponse, 
                                                         boolean addCOOP, boolean addCOEP) {
        // Access Token 쿠키 설정
        var accessTokenCookie = cookieUtils.createAccessTokenCookie(
                tokenResponse.accessToken(), 
                tokenResponse.accessTokenExpiresIn()
        );

        // Refresh Token 쿠키 설정
        var refreshTokenCookie = tokenResponse.refreshToken() != null
                ? cookieUtils.createRefreshTokenCookie(
                        tokenResponse.refreshToken(), 
                        tokenResponse.refreshTokenExpiresIn()
                )
                : null;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, redirectUrl);
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        
        if (refreshTokenCookie != null) {
            headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        }

        // CORS 관련 헤더 추가
        if (addCOOP) {
            headers.add("Cross-Origin-Opener-Policy", "same-origin-allow-popups");
        }
        if (addCOEP) {
            headers.add("Cross-Origin-Embedder-Policy", "unsafe-none");
        }

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }

    private ResponseEntity<String> getErrorRedirectResponse(String baseRedirectUri) {
        return getErrorRedirectResponse(baseRedirectUri, "로그인 처리 중 오류가 발생했습니다.");
    }

    private ResponseEntity<String> getErrorRedirectResponse(String baseRedirectUri, String errorMessage) {
        try {
            String redirectUrl = baseRedirectUri + "?success=false&error=" +
                    java.net.URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

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
            
            var accessTokenCookie = cookieUtils.deleteAccessTokenCookie();
            var refreshTokenCookie = cookieUtils.deleteRefreshTokenCookie();
            
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
            if (StringUtils.isBlank(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "REFRESH_TOKEN_REQUIRED", "message", "Refresh Token이 필요합니다."));
            }

            TokenResponse tokenResponse = tokenService.refreshAccessToken(refreshToken);

            var accessTokenCookie = cookieUtils.createAccessTokenCookie(
                    tokenResponse.accessToken(), 
                    tokenResponse.accessTokenExpiresIn()
            );
            var refreshTokenCookie = cookieUtils.createRefreshTokenCookie(
                    tokenResponse.refreshToken(), 
                    tokenResponse.refreshTokenExpiresIn()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(tokenResponse);
        } catch (IllegalArgumentException e) {
            log.warn("토큰 갱신 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "INVALID_REFRESH_TOKEN", "message", e.getMessage()));
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            log.warn("토큰 갱신 중 동시성 오류 (이미 갱신됨 또는 중복 요청): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "REFRESH_TOKEN_CONFLICT", "message", "토큰이 이미 갱신되었습니다. 새로고침 후 다시 시도하거나 재로그인 해주세요."));
        } catch (Exception e) {
            log.error("토큰 갱신 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "INTERNAL_ERROR", "message", "토큰 갱신 중 오류가 발생했습니다."));
        }
    }

    // TODO 토큰 발급 TEST EndPoint - 개발용
    @GetMapping("/jwt/token")
    public ResponseEntity<String> getJwtToken(@RequestParam("user_id") Long userId) {
        try {
            TokenResponse tokenResponse = tokenService.loginWithUserId(userId);
            return ResponseEntity.ok().body(tokenResponse.accessToken());
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
            TokenResponse tokenResponse = tokenService.loginWithTestAccount(request.getId());

            var accessTokenCookie = cookieUtils.createAccessTokenCookie(
                    tokenResponse.accessToken(), 
                    tokenResponse.accessTokenExpiresIn()
            );
            var refreshTokenCookie = cookieUtils.createRefreshTokenCookie(
                    tokenResponse.refreshToken(), 
                    tokenResponse.refreshTokenExpiresIn()
            );

            log.info("테스트 로그인 성공: memberId={}", request.getId());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(Map.of(
                            "message", "테스트 로그인 성공",
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