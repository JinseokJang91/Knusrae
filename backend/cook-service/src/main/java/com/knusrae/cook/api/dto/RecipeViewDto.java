package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeView;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeViewDto {
    private Long id;
    private Long memberId;
    private Long recipeId;
    private LocalDateTime viewedAt;
    
    // 레시피 정보 (조인 시 포함)
    private RecipeSimpleDto recipe;
    
    public static RecipeViewDto from(RecipeView view) {
        return RecipeViewDto.builder()
                .id(view.getId())
                .memberId(view.getMemberId())
                .recipeId(view.getRecipeId())
                .viewedAt(view.getViewedAt())
                .build();
    }
    
    public static RecipeViewDto from(RecipeView view, RecipeSimpleDto recipe) {
        return RecipeViewDto.builder()
                .id(view.getId())
                .memberId(view.getMemberId())
                .recipeId(view.getRecipeId())
                .viewedAt(view.getViewedAt())
                .recipe(recipe)
                .build();
    }
}
