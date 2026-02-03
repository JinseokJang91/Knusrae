package com.knusrae.cook.api.ingredient.dto;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientPreparation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientPreparationDto {
    private Long id;
    private IngredientDto ingredient;
    private String content;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static IngredientPreparationDto fromEntity(IngredientPreparation preparation) {
        if (preparation == null) {
            return null;
        }
        return IngredientPreparationDto.builder()
                .id(preparation.getId())
                .ingredient(IngredientDto.fromEntity(preparation.getIngredient()))
                .content(preparation.getContent())
                .summary(preparation.getSummary())
                .createdAt(preparation.getCreatedAt())
                .updatedAt(preparation.getUpdatedAt())
                .build();
    }
}
