package com.knusrae.member.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로워 목록 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowerDto {
    private Long followId;
    private Long followerId;
    private String followerNickname;
    private String followerProfileImage;
    private LocalDateTime createdAt;
}
