package com.knusrae.auth.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knusrae.auth.auth.dto.NaverUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NaverAuthService {
    @Value("${naver.client.id}")
    private String clientId;
    @Value("${naver.client.secret}")
    private String clientSecret;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NaverAuthService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public NaverUserDTO naverLoginProcess(String code, String state) throws Exception {
        // 액세스 토큰 요청
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String tokenRequestBody = String.format(
                "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&state=%s",
                clientId, clientSecret, code, state
        );
        HttpEntity<String> tokenRequest = new HttpEntity<>(tokenRequestBody, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, String.class);

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 사용자 정보 요청
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                userInfoUrl, HttpMethod.GET, userInfoRequest, String.class
        );

        JsonNode userInfoJson = objectMapper.readTree(userInfoResponse.getBody());
        JsonNode user = userInfoJson.get("response");
        return objectMapper.treeToValue(user, NaverUserDTO.class);
    }
}
