package com.knusrae.cook.api.recipe.dto;

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
    private int totalLikes;
    private boolean isLiked;
    private long favoriteCount;
}
