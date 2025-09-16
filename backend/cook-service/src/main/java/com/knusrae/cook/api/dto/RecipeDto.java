package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.Recipe;
import lombok.Getter;

@Getter
public class RecipeDto {
    private final Long id;
    private final String title;
    private final String category;
    private final Long hits;
    private final String state;
    private final Long userId;

    public RecipeDto(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.category = recipe.getCategory();
        this.hits = recipe.getHits();
        this.state = recipe.getState().name();
        this.userId = recipe.getUserId();
    }


    public Recipe toEntity() {
        return Recipe.builder()
                .id(id)
                .title(title)
                .category(category)
                .hits(hits)
                .state(CookState.valueOf(state))
                .userId(userId)
                .build();
    }
}
