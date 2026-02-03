package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipeDetail;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeStepDto {
    private Long order;

    @NotBlank(message = "조리 단계 설명은 필수입니다.")
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
