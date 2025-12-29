package com.knusrae.cook.api.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeStatsDto {
    private int totalComments;
    private int totalReviews;
    private double averageRating;
    private int totalLikes; // TODO: 좋아요 기능 추가 시
    private boolean isLiked; // TODO: 현재 사용자가 좋아요 했는지
    private long favoriteCount; // 찜 개수
}
