package com.knusrae.cook.api.popular.dto;

import com.knusrae.cook.api.recipe.dto.RecipeDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularRecipeDto {
    private Integer rank;
    private Integer previousRank;
    private String trendStatus; // UP, DOWN, NEW, SAME
    private RecipeDto recipe;
    private PopularityStatsDto popularityStats;
    private LocalDateTime calculatedAt;
}

