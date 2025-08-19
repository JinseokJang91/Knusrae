package com.knusrae.auth.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.dto.NaverUserDTO;
import com.knusrae.auth.auth.service.NaverAuthService;
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
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final NaverAuthService naverAuthService;
    private final ObjectMapper objectMapper;

    private static final String REDIRECT_URI = "http://localhost:5173/auth/naver/callback";

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
}