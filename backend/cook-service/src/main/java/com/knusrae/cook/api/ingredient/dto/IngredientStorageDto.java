package com.knusrae.cook.api.ingredient.dto;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientStorage;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientStorageDto {
    private Long id;
    private IngredientDto ingredient;
    private String content;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IngredientStorageDto fromEntity(IngredientStorage storage) {
        if (storage == null) {
            return null;
        }
        return IngredientStorageDto.builder()
                .id(storage.getId())
                .ingredient(IngredientDto.fromEntity(storage.getIngredient()))
                .content(storage.getContent())
                .summary(storage.getSummary())
                .createdAt(storage.getCreatedAt())
                .updatedAt(storage.getUpdatedAt())
                .build();
    }
}
