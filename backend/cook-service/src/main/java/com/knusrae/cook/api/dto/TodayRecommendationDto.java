package com.knusrae.cook.api.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayRecommendationDto {
    private List<RecommendedRecipeDto> recipes;
    private String recommendationType; // PERSONALIZED | GENERAL
    private Boolean refreshable;
}
