package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.Ingredient;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientDto {
    private Long id;
    private String name;
    private IngredientGroupDto group;
    private String imageUrl;
    private Integer sortOrder;
    
    public static IngredientDto fromEntity(Ingredient ingredient) {
        if (ingredient == null) {
            return null;
        }
        return IngredientDto.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .group(IngredientGroupDto.fromEntity(ingredient.getGroup()))
                .imageUrl(ingredient.getImageUrl())
                .sortOrder(ingredient.getSortOrder())
                .build();
    }
}
