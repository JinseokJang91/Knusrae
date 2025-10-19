package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeStepDto {
    private Long order;
    private String text;

    public static RecipeStepDto fromEntity(RecipeDetail recipeDetail) {
        return RecipeStepDto.builder()
                .order(recipeDetail.getStep())
                .text(recipeDetail.getContent())
                .build();
    }
}
