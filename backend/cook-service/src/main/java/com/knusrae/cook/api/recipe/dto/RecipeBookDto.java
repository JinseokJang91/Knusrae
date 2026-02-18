package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBook;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeBookDto {
    private Long id;
    private Long memberId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Long bookmarkCount; // 레시피북 내 북마크 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecipeBookDto from(RecipeBook recipeBook) {
        return RecipeBookDto.builder()
                .id(recipeBook.getId())
                .memberId(recipeBook.getMemberId())
                .name(recipeBook.getName())
                .color(recipeBook.getColor())
                .sortOrder(recipeBook.getSortOrder())
                .createdAt(recipeBook.getCreatedAt())
                .updatedAt(recipeBook.getUpdatedAt())
                .build();
    }

    public static RecipeBookDto from(RecipeBook recipeBook, Long bookmarkCount) {
        return RecipeBookDto.builder()
                .id(recipeBook.getId())
                .memberId(recipeBook.getMemberId())
                .name(recipeBook.getName())
                .color(recipeBook.getColor())
                .sortOrder(recipeBook.getSortOrder())
                .bookmarkCount(bookmarkCount)
                .createdAt(recipeBook.getCreatedAt())
                .updatedAt(recipeBook.getUpdatedAt())
                .build();
    }
}
