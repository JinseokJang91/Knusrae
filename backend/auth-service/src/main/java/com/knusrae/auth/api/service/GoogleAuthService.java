package com.knusrae.auth.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.api.domain.Member;
import com.knusrae.auth.api.dto.GoogleUserDTO;
import com.knusrae.auth.api.dto.MemberState;
import com.knusrae.auth.api.dto.SocialRole;
import com.knusrae.auth.api.repository.MemberRepository;
import com.knusrae.auth.api.service.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    public TokenResponse googleLoginProcess(String code) throws JsonProcessingException {
        // 액세스 토큰 요청
        String accessToken = getAccessToken(code);

        // 사용자 정보 요청
        GoogleUserDTO googleUserDTO = getUserInfo(accessToken);

        // 1. DB에서 사용자 조회/없으면 생성
        Member member = memberRepository.findByEmail(googleUserDTO.getEmail());

        if(ObjectUtils.isEmpty(member)) {
            member = memberRepository.save(
                    Member.builder()
                            .email(googleUserDTO.getEmail())
                            .name(googleUserDTO.getName())
                            .state(MemberState.ACTIVE)
                            .role(SocialRole.GOOGLE)
                            .createdAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .build()
            );
        }

        // 2. JWT 토큰 발급 (ID, role 사용)
        return tokenService.loginWithSocialUser(member.getId(), member.getName(), member.getRole().name());
    }

    private String getAccessToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String tokenRequestBody = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                clientId, clientSecret, code, redirectUri
        );
        HttpEntity<String> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(TOKEN_URL, tokenRequest, String.class);

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        return tokenJson.get("access_token").asText();
    }

    private GoogleUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        return objectMapper.readValue(userInfoResponse.getBody(), GoogleUserDTO.class);
    }
}