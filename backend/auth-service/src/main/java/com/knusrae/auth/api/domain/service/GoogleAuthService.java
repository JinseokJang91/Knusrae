package com.knusrae.auth.api.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.api.dto.GoogleUserDTO;
import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.enums.Active;
import com.knusrae.common.domain.enums.SocialRole;
import com.knusrae.common.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
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

        // 1. DB에서 사용자 조회/없으면 생성 (이메일 + 소셜 역할로 조회)
        Member member = memberRepository.findByEmailAndSocialRole(googleUserDTO.getEmail(), SocialRole.GOOGLE);

        if(ObjectUtils.isEmpty(member)) {
            member = memberRepository.save(
                    Member.builder()
                            .name(googleUserDTO.getName())
                            .nickname(googleUserDTO.getName())
                            .email(googleUserDTO.getEmail())
                            .profileImage(googleUserDTO.getPicture())
                            .isActive(Active.TRUE)
                            .socialRole(SocialRole.GOOGLE)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            );
        }

        // 2. JWT 토큰 발급 (ID, role 사용)
        return tokenService.loginWithSocialUser(member.getId(), member.getName(), member.getSocialRole().name());
    }

    private String getAccessToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(TOKEN_URL, tokenRequest, String.class);

        // 아래는 아까와 동일한 방어 코드
        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google token API error: " + tokenResponse.getStatusCode() +
                    " / body=" + tokenResponse.getBody());
        }

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        JsonNode accessTokenNode = tokenJson.get("access_token");
        if (accessTokenNode == null) {
            throw new RuntimeException("Failed to get access_token from Google: " + tokenResponse.getBody());
        }

        return accessTokenNode.asText();
    }

    private GoogleUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google user info API error: " +
                    userInfoResponse.getStatusCode() + " / body=" + userInfoResponse.getBody());
        }

        return objectMapper.readValue(userInfoResponse.getBody(), GoogleUserDTO.class);
    }
}