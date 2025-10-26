package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeImageDto {
    private Long id;
    private String url;
    private String fileName;
    private String contentType;
    private Long size;
    private Integer sortOrder;
    private boolean isMainImage;
    private LocalDateTime createdAt;

    public static RecipeImageDto fromEntity(RecipeImage recipeImage) {
        return RecipeImageDto.builder()
                .id(recipeImage.getId())
                .url(recipeImage.getUrl())
                .fileName(recipeImage.getFileName())
                .contentType(recipeImage.getContentType())
                .size(recipeImage.getSize())
                .sortOrder(recipeImage.getSortOrder())
                .isMainImage(recipeImage.isMainImage())
                .createdAt(recipeImage.getCreatedAt())
                .build();
    }
}
