package com.knusrae.member.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로잉 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowingDto {
    private Long followId;
    private Long followingId;
    private String followingNickname;
    private String followingProfileImage;
    private LocalDateTime createdAt;
}
