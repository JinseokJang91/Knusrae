package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeStepDetailDto {
    private Long id;
    private Long step;
    private String content;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecipeStepDetailDto fromEntity(RecipeDetail recipeDetail) {
        return RecipeStepDetailDto.builder()
                .id(recipeDetail.getId())
                .step(recipeDetail.getStep())
                .content(recipeDetail.getContent())
                .image(recipeDetail.getImage())
                .createdAt(recipeDetail.getCreatedAt())
                .updatedAt(recipeDetail.getUpdatedAt())
                .build();
    }
}
