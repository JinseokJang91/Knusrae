package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipeFavorite;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeFavoriteDto {
    private Long id;
    private Long memberId;
    private Long recipeId;
    private LocalDateTime createdAt;
    private RecipeSimpleDto recipe;

    public static RecipeFavoriteDto from(RecipeFavorite favorite) {
        return RecipeFavoriteDto.builder()
                .id(favorite.getId())
                .memberId(favorite.getMemberId())
                .recipeId(favorite.getRecipeId())
                .createdAt(favorite.getCreatedAt())
                .build();
    }

    public static RecipeFavoriteDto from(RecipeFavorite favorite, RecipeSimpleDto recipeDto) {
        return RecipeFavoriteDto.builder()
                .id(favorite.getId())
                .memberId(favorite.getMemberId())
                .recipeId(favorite.getRecipeId())
                .createdAt(favorite.getCreatedAt())
                .recipe(recipeDto)
                .build();
    }
}
