package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeDetail;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeStepDto {
    private Long order;
    private String text;

    public static RecipeStepDto fromEntity(RecipeDetail recipeDetail) {
        return RecipeStepDto.builder()
                .order(recipeDetail.getStep())
                .text(recipeDetail.getDescription())
                .build();
    }

    public RecipeDetail toEntity(RecipeStepDto recipeStepDto) {
        return RecipeDetail.builder()
                .step(recipeStepDto.getOrder())
                .description(recipeStepDto.getText())
                .build();
    }
}
