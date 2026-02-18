package com.knusrae.cook.api.creator.dto;

import com.knusrae.common.domain.entity.Member;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorDto {

    private Long memberId;
    private String nickname;
    private String profileImage;
    private String bio;
    private long followerCount;
    private long followingCount;
    private long recipeCount;
    private long totalHits;
    private long totalFavoriteCount;
    private boolean isFollowing;
    private String recommendReason;

    public static CreatorDto from(Member member, long recipeCount, long totalHits,
                                   long totalFavoriteCount, boolean isFollowing, String recommendReason) {
        return CreatorDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .bio(member.getBio())
                .followerCount(member.getFollowerCount())
                .followingCount(member.getFollowingCount())
                .recipeCount(recipeCount)
                .totalHits(totalHits)
                .totalFavoriteCount(totalFavoriteCount)
                .isFollowing(isFollowing)
                .recommendReason(recommendReason)
                .build();
    }
}
