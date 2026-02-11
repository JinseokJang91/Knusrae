package com.knusrae.cook.api.recipe.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCommentItemDto {
    private RecipeCommentDto comment;
    private Long recipeId;
    private String recipeTitle;
    private String recipeThumbnail;
}
