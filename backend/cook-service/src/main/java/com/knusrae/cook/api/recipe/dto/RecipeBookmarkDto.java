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
    private Long folderId;
    private Long recipeId;
    private Long memberId;
    private LocalDateTime createdAt;
    private RecipeSimpleDto recipe; // 레시피 정보

    public static RecipeBookmarkDto from(RecipeBookmark bookmark) {
        return RecipeBookmarkDto.builder()
                .id(bookmark.getId())
                .folderId(bookmark.getFolderId())
                .recipeId(bookmark.getRecipeId())
                .memberId(bookmark.getMemberId())
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    public static RecipeBookmarkDto from(RecipeBookmark bookmark, RecipeSimpleDto recipeDto) {
        return RecipeBookmarkDto.builder()
                .id(bookmark.getId())
                .folderId(bookmark.getFolderId())
                .recipeId(bookmark.getRecipeId())
                .memberId(bookmark.getMemberId())
                .createdAt(bookmark.getCreatedAt())
                .recipe(recipeDto)
                .build();
    }
}
