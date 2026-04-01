package com.knusrae.member.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    private Long id;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String profileImage;
    private String bio;
    private Long followerCount;
    private Long followingCount;
    /**
     * 관리자 여부 (서버에서 판별하여 내려주는 플래그)
     * JsonProperty로 키를 isAdmin으로 고정 (boolean getter가 JSON에서 "admin"으로 나가는 것 방지)
     */
    @JsonProperty("isAdmin")
    private boolean isAdmin;
}
