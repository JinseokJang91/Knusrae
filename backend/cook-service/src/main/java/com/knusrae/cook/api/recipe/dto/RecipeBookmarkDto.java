package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBookmark;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeBookmarkDto {
    private Long id;
    private Long recipeBookId;
    private Long recipeId;
    private Long memberId;
    private String memo;
    private LocalDateTime createdAt;
    private RecipeSimpleDto recipe; // 레시피 정보

    public static RecipeBookmarkDto from(RecipeBookmark bookmark) {
        return RecipeBookmarkDto.builder()
                .id(bookmark.getId())
                .recipeBookId(bookmark.getRecipeBookId())
                .recipeId(bookmark.getRecipeId())
                .memberId(bookmark.getMemberId())
                .memo(bookmark.getMemo())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    public static RecipeBookmarkDto from(RecipeBookmark bookmark, RecipeSimpleDto recipeDto) {
        return RecipeBookmarkDto.builder()
                .id(bookmark.getId())
                .recipeBookId(bookmark.getRecipeBookId())
                .recipeId(bookmark.getRecipeId())
                .memberId(bookmark.getMemberId())
                .memo(bookmark.getMemo())
                .createdAt(bookmark.getCreatedAt())
                .recipe(recipeDto)
                .build();
    }
}
