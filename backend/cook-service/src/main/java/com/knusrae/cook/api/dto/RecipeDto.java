package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.Recipe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecipeDto {
    private final Long id;

    @NotBlank(message = "레시피 제목은 필수입니다.")
    private final String title;

    @NotBlank(message = "카테고리는 필수입니다.")
    private final String category;

    private final Long hits;
    private final String state;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private final Long userId;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RecipeDto(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.category = recipe.getCategory();
        this.hits = recipe.getHits();
        this.state = recipe.getState().name();
        this.userId = recipe.getUserId();
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();
    }

    // 생성자 - @Builder와 함께 사용
    public RecipeDto(Long id, String title, String category, Long hits, String state, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.hits = hits;
        this.state = state;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .id(id)
                .title(title)
                .category(category)
                .hits(hits != null ? hits : 0L)
                .state(state != null ? CookState.valueOf(state) : CookState.ACTIVE)
                .userId(userId)
                .build();
    }

    // 생성용 DTO
    public static RecipeDto createDto(String title, String category, Long userId) {
        return RecipeDto.builder()
                .title(title)
                .category(category)
                .userId(userId)
                .hits(0L)
                .state(CookState.ACTIVE.name())
                .build();
    }

    // 업데이트용 DTO
    public static RecipeDto updateDto(Long id, String title, String category, CookState state) {
        return RecipeDto.builder()
                .id(id)
                .title(title)
                .category(category)
                .state(state.name())
                .build();
    }
}
