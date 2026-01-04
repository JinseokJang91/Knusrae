package com.knusrae.auth.api.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.api.dto.KakaoUserDTO;
import com.knusrae.auth.api.web.response.TokenResponse;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.enums.Active;
import com.knusrae.common.domain.enums.SocialRole;
import com.knusrae.common.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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
public class KakaoAuthService {
    @Value("${kakao.client.id}")
    private String clientId;
    @Value("${kakao.client.secret}")
    private String clientSecret;
    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public TokenResponse kakaoLoginProcess(String code) throws JsonProcessingException {
        // 액세스 토큰 요청
        String accessToken = getAccessToken(code);

        // 사용자 정보 요청
        KakaoUserDTO kakaoUserDTO = getUserInfo(accessToken);

        // 1. DB에서 사용자 조회/없으면 생성 (이메일 + 소셜 역할로 조회)
        Member member = memberRepository.findByEmailAndSocialRole(kakaoUserDTO.getEmail(), SocialRole.KAKAO);

        if(ObjectUtils.isEmpty(member)) {
            String profileImage = kakaoUserDTO.getProperties() != null ? kakaoUserDTO.getProperties().getProfileImage() : null;
            member = memberRepository.save(
                    Member.builder()
                            .name(kakaoUserDTO.getName())
                            .nickname(kakaoUserDTO.getNickname())
                            .phone(StringUtils.replaceChars(kakaoUserDTO.getPhoneNumber(), "+82 ", "0").replace("-", ""))
                            .email(kakaoUserDTO.getEmail())
                            .birth(Strings.concat(kakaoUserDTO.getBirthyear(), kakaoUserDTO.getBirthday()))
                            .profileImage(profileImage)
                            .isActive(Active.TRUE)
                            .socialRole(SocialRole.KAKAO)
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

        ResponseEntity<String> tokenResponse =
                restTemplate.postForEntity(TOKEN_URL, tokenRequest, String.class);

        // 1) HTTP Status 체크
        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Kakao token API error: " +
                    tokenResponse.getStatusCode() + " / body=" + tokenResponse.getBody());
        }

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());

        // 2) access_token 존재 여부 체크
        JsonNode accessTokenNode = tokenJson.get("access_token");
        if (accessTokenNode == null || accessTokenNode.isNull()) {
            throw new RuntimeException("Failed to get access_token from Kakao: " + tokenResponse.getBody());
        }

        return accessTokenNode.asText();
    }


    private KakaoUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Kakao user info API error: " +
                    userInfoResponse.getStatusCode() + " / body=" + userInfoResponse.getBody());
        }

        return objectMapper.readValue(userInfoResponse.getBody(), KakaoUserDTO.class);
    }
}