package com.knusrae.auth.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoUserDTO {
    private String id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;

    @Data
    public static class KakaoAccount {
        private String email;
        private String name;
        @JsonProperty("phone_number")
        private String phoneNumber;
        @JsonProperty("email_verified")
        private boolean emailVerified;
    }

    @Data
    public static class Properties {
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
        @JsonProperty("thumbnail_image")
        private String thumbnailImage;
    }

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.getEmail() : null;
    }

    public String getName() {
        return kakaoAccount != null ? kakaoAccount.getName() : null;
    }

    public String getNickname() {
        return properties != null ? properties.getNickname() : null;
    }

    public String getPhoneNumber() {
        return kakaoAccount != null ? kakaoAccount.getPhoneNumber() : null;
    }
}