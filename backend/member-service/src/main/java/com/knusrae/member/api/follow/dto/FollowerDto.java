package com.knusrae.member.api.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
