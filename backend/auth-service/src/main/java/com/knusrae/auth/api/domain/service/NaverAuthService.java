package com.knusrae.auth.api.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.enums.Active;
import com.knusrae.common.domain.enums.Gender;
import com.knusrae.auth.api.dto.NaverUserDTO;
import com.knusrae.common.domain.enums.SocialRole;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.auth.api.web.response.TokenResponse;
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
public class NaverAuthService {
    @Value("${naver.client.id}")
    private String clientId;
    @Value("${naver.client.secret}")
    private String clientSecret;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public TokenResponse naverLoginProcess(String code, String state) throws JsonProcessingException {
        // 액세스 토큰 요청
        String accessToken = getAccessToken(code, state);

        // 사용자 정보 요청
        NaverUserDTO naverUserDTO = getUserInfo(accessToken);

        // 1. DB에서 사용자 조회/없으면 생성 (이메일 + 소셜 역할로 조회)
        Member member = memberRepository.findByEmailAndSocialRole(naverUserDTO.getEmail(), SocialRole.NAVER);

        if(ObjectUtils.isEmpty(member)) {
            member = memberRepository.save(
                    Member.builder()
                            .name(naverUserDTO.getName())
                            .nickname(naverUserDTO.getNickname())
                            .phone( StringUtils.replaceChars(naverUserDTO.getMobile(), "-", ""))
                            .email(naverUserDTO.getEmail())
                            .profileImage(naverUserDTO.getProfileImage())
                            .isActive(Active.TRUE)
                            .birth(Strings.concat(naverUserDTO.getBirthyear(), StringUtils.replaceChars(naverUserDTO.getBirthday(), "-", "")))
                            .gender("M".equals(naverUserDTO.getGender()) ? Gender.MALE : "F".equals(naverUserDTO.getGender()) ? Gender.FEMALE : Gender.UNKNOWN)
                            .socialRole(SocialRole.NAVER)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            );
        }

        // 2. JWT 토큰 발급 (ID, role 사용)
        return tokenService.loginWithSocialUser(member.getId(), member.getName(), member.getSocialRole().name());
    }

    private String getAccessToken(String code, String state) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);

        ResponseEntity<String> tokenResponse =
                restTemplate.postForEntity(TOKEN_URL, tokenRequest, String.class);

        // 1) HTTP Status 체크
        if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Naver token API error: " +
                    tokenResponse.getStatusCode() + " / body=" + tokenResponse.getBody());
        }

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());

        // 2) access_token 존재 여부 체크
        JsonNode accessTokenNode = tokenJson.get("access_token");
        if (accessTokenNode == null || accessTokenNode.isNull()) {
            throw new RuntimeException("Failed to get access_token from Naver: " + tokenResponse.getBody());
        }

        return accessTokenNode.asText();
    }


    private NaverUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Naver user info API error: " +
                    userInfoResponse.getStatusCode() + " / body=" + userInfoResponse.getBody());
        }

        // NAVER : response 객체에 사용자 정보 존재
        JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());
        JsonNode user = userInfoJson.get("response");

        if (user == null || user.isNull()) {
            throw new RuntimeException("Failed to get user info from Naver: " + userInfoResponse.getBody());
        }

        return objectMapper.treeToValue(user, NaverUserDTO.class);
    }

}
