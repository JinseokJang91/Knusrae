package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipeDetail;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeStepDetailDto {
    private Long id;
    private Long step;
    private String description;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecipeStepDetailDto fromEntity(RecipeDetail recipeDetail) {
        return RecipeStepDetailDto.builder()
                .id(recipeDetail.getId())
                .step(recipeDetail.getStep())
                .description(recipeDetail.getDescription())
                .image(recipeDetail.getImage())
                .createdAt(recipeDetail.getCreatedAt())
                .updatedAt(recipeDetail.getUpdatedAt())
                .build();
    }
}
