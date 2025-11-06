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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
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

        // 1. DB에서 사용자 조회/없으면 생성
        Member member = memberRepository.findByEmail(naverUserDTO.getEmail());
        
        if(ObjectUtils.isEmpty(member)) {
            member = memberRepository.save(
                    Member.builder()
                            .name(naverUserDTO.getName())
                            .nickname(naverUserDTO.getNickname())
                            .phone( StringUtils.replaceChars(naverUserDTO.getMobile(), "-", ""))
                            .email(naverUserDTO.getEmail())
                            .isActive(Active.TRUE)
                            .birth(Strings.concat(naverUserDTO.getBirthyear(), StringUtils.replaceChars(naverUserDTO.getBirthday(), "-", "")))
                            .gender("M".equals(naverUserDTO.getGender()) ? Gender.FEMALE : "F".equals(naverUserDTO.getGender()) ? Gender.MALE : Gender.UNKNOWN)
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

        String tokenRequestBody = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&state=%s",
                clientId, clientSecret, code, state
        );
        HttpEntity<String> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(TOKEN_URL, tokenRequest, String.class);

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());

        return tokenJson.get("access_token").asText();
    }

    private NaverUserDTO getUserInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                USER_INFO_URL, HttpMethod.GET, userInfoRequest, String.class
        );

        JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());
        JsonNode user = userInfoJson.get("response");

        return objectMapper.treeToValue(user, NaverUserDTO.class);
    }
}
