package com.knusrae.cook.api.ingredient.dto;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientGroup;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientGroupDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer sortOrder;

    public static IngredientGroupDto fromEntity(IngredientGroup group) {
        if (group == null) {
            return null;
        }
        return IngredientGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .imageUrl(group.getImageUrl())
                .sortOrder(group.getSortOrder())
                .build();
    }
}
