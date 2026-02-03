package com.knusrae.cook.api.ingredient.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class IngredientListResponseDto {
    private List<IngredientGroupDto> groups;
    private List<IngredientDto> ingredients;
    private Long totalCount;
}
