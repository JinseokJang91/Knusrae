package com.knusrae.member.api.follow.dto;

import com.knusrae.common.domain.entity.Follow;
import com.knusrae.member.api.member.dto.MemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowDto {
    private Long id;
    private Long followerId;
    private Long followingId;
    private LocalDateTime createdAt;
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
