package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.Recipe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecipeDto {
    private final Long id;

    @NotBlank(message = "레시피 제목은 필수입니다.")
    private final String title;

    @NotBlank(message = "레시피 설명은 필수입니다.")
    private final String description;

    @NotBlank(message = "카테고리는 필수입니다.")
    private final String category;

    private final String status;
    private final String visibility;

    private final Long hits;

    @NotNull(message = "회원 ID는 필수입니다.")
    private final Long memberId;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RecipeDto(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.category = recipe.getCategory();
        this.status = recipe.getStatus().name();
        this.visibility = recipe.getVisibility().name();
        this.hits = recipe.getHits();
        this.memberId = recipe.getMemberId();
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();
        this.description = recipe.getDescription();
    }

    @Builder
    public RecipeDto(Long id, String title, String description, String category, Long hits, String status, String visibility, Long memberId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.hits = hits;
        this.status = status;
        this.visibility = visibility;
        this.memberId = memberId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Recipe toEntity() {
        return Recipe.builder()
                .id(id)
                .title(title)
                .description(description)
                .category(category)
                .status(Status.valueOf(status))
                .visibility(Visibility.valueOf(visibility))
                .hits(hits != null ? hits : 0L)
                .memberId(memberId)
                .build();
    }

    // 생성용 DTO
    public static RecipeDto createDto(String title, String description, String category, Long memberId) {
        return RecipeDto.builder()
                .title(title)
                .description(description)
                .category(category)
                .status(Status.DRAFT.name())
                .visibility(Visibility.PUBLIC.name())
                .memberId(memberId)
                .build();
    }

    // 업데이트용 DTO
    public static RecipeDto updateDto(Long id, String title, String description, String category, String status, String visibility) {
        return RecipeDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .visibility(visibility)
                .build();
    }
}
