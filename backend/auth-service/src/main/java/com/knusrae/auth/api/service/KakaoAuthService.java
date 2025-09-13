package com.knusrae.auth.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.api.domain.Member;
import com.knusrae.auth.api.dto.KakaoUserDTO;
import com.knusrae.auth.api.dto.MemberState;
import com.knusrae.auth.api.dto.SocialRole;
import com.knusrae.auth.api.repository.MemberRepository;
import com.knusrae.auth.api.service.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
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

        // 1. DB에서 사용자 조회/없으면 생성
        Member member = memberRepository.findByEmail(kakaoUserDTO.getEmail());

        if(ObjectUtils.isEmpty(member)) {
            member = memberRepository.save(
                    Member.builder()
                            .email(kakaoUserDTO.getEmail())
                            .name(StringUtils.isNotBlank(kakaoUserDTO.getName()) ? kakaoUserDTO.getName() : kakaoUserDTO.getNickname())
                            .phone(StringUtils.replaceChars(kakaoUserDTO.getPhoneNumber(), "+82 ", "0").replace("-", ""))
                            .birth(Strings.concat(kakaoUserDTO.getBirthyear(), kakaoUserDTO.getBirthday()))
                            .state(MemberState.ACTIVE)
                            .role(SocialRole.KAKAO)
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

    private KakaoUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        return objectMapper.readValue(userInfoResponse.getBody(), KakaoUserDTO.class);
    }
}