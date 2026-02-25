package com.knusrae.cook.api.popular.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipePopularity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularityStatsDto {
    private Double popularityScore;
    private Long hits24h;
    private Long hits7d;
    private Long hits30d;
    private Long favoriteCount;
    private Long commentCount;
    private Long favoriteIncrease24h;
    
    public static PopularityStatsDto from(RecipePopularity popularity) {
        if (popularity == null) {
            return PopularityStatsDto.builder()
                    .popularityScore(0.0)
                    .hits24h(0L)
                    .hits7d(0L)
                    .hits30d(0L)
                    .favoriteCount(0L)
                    .commentCount(0L)
                    .favoriteIncrease24h(0L)
                    .build();
        }
        
        return PopularityStatsDto.builder()
                .popularityScore(popularity.getPopularityScore())
                .hits24h(popularity.getHits24h())
                .hits7d(popularity.getHits7d())
                .hits30d(popularity.getHits30d())
                .favoriteCount(popularity.getFavoriteCount())
                .commentCount(popularity.getCommentCount())
                .favoriteIncrease24h(popularity.getFavoriteIncrease24h())
                .build();
    }
}

