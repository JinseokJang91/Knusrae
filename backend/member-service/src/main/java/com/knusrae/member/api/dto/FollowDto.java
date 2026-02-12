package com.knusrae.member.api.dto;

import com.knusrae.common.domain.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔로우 관계 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDto {
    private Long id;
    private Long followerId;
    private Long followingId;
    private LocalDateTime createdAt;
    
    // 회원 정보 (조인 시 포함)
    private MemberDto follower;
    private MemberDto following;
    
    public static FollowDto from(Follow follow) {
        return FollowDto.builder()
                .id(follow.getId())
                .followerId(follow.getFollowerId())
                .followingId(follow.getFollowingId())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
