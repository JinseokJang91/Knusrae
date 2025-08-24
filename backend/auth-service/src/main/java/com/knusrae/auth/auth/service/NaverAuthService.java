package com.knusrae.auth.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.domain.User;
import com.knusrae.auth.auth.dto.NaverUserDTO;
import com.knusrae.auth.auth.dto.SocialRole;
import com.knusrae.auth.auth.repository.UserRepository;
import com.knusrae.auth.auth.service.request.TokenRequest;
import com.knusrae.auth.auth.service.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private final UserRepository userRepository;

    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public TokenResponse naverLoginProcess(String code, String state) throws JsonProcessingException {
        // 액세스 토큰 요청
        String accessToken = getAccessToken(code, state);

        // 사용자 정보 요청
        NaverUserDTO naverUserDTO = getUserInfo(accessToken);

        // 1. DB에서 사용자 조회/없으면 생성
        User user = (User) userRepository.findByEmail(naverUserDTO.getEmail())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .name(naverUserDTO.getName())
                                .phone(naverUserDTO.getMobile())
                                .role(SocialRole.NAVER)
                                .build()
                 ));
        System.out.println("user = " + user);

        // 2. JWT 토큰 발급 (ID, role 사용)
        return tokenService.loginWithSocialUser(user.getId(), user.getRole().name());
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
