package com.knusrae.cook.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class RecipeDto {
    private Long id;

    @NotBlank(message = "레시피 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "레시피 설명은 필수입니다.")
    private String description;

    private List<RecipeCategoryDto> categories = new java.util.ArrayList<>();

    private String status;
    private String visibility;
    private String thumbnail;

    private Long hits;

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    private List<RecipeStepDto> steps;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public RecipeDto(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.status = recipe.getStatus().name();
        this.visibility = recipe.getVisibility().name();
        this.thumbnail = recipe.getThumbnail();
        this.hits = recipe.getHits();
        this.memberId = recipe.getMemberId();
        this.steps = recipe.getRecipeDetails().stream()
                .map(RecipeStepDto::fromEntity)
                .toList();
        this.createdAt = recipe.getCreatedAt();
        this.updatedAt = recipe.getUpdatedAt();
        this.description = recipe.getDescription();
        this.categories = recipe.getRecipeCategories().stream()
                .map(RecipeCategoryDto::fromEntity)
                .toList();
    }

    @Builder
    public RecipeDto(Long id, String title, String description, List<RecipeCategoryDto> categories, Long hits, String status, String visibility, Long memberId, List<RecipeStepDto> steps, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.categories = categories != null ? categories : new java.util.ArrayList<>();
        this.hits = hits;
        this.status = status;
        this.visibility = visibility;
        this.memberId = memberId;
        this.steps = steps;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Recipe toEntity() {
        Status statusValue = status != null ? Status.valueOf(status) : Status.DRAFT;
        Visibility visibilityValue = visibility != null ? Visibility.valueOf(visibility) : Visibility.PUBLIC;

        return Recipe.builder()
                .id(id)
                .title(title)
                .description(description)
                .status(statusValue)
                .visibility(visibilityValue)
                .hits(hits != null ? hits : 0L)
                .memberId(memberId)
                .build();
    }

    public List<RecipeCategoryDto> getCategories() {
        if (categories == null) {
            categories = new java.util.ArrayList<>();
        }
        return categories;
    }

    // 생성용 DTO
    public static RecipeDto createDto(String title, String description, List<RecipeCategoryDto> categories, Long memberId, List<RecipeStepDto> steps) {
        return RecipeDto.builder()
                .title(title)
                .description(description)
                .categories(categories != null ? categories : new java.util.ArrayList<>())
                .status(Status.DRAFT.name())
                .visibility(Visibility.PUBLIC.name())
                .memberId(memberId)
                .steps(steps)
                .build();
    }

    // 업데이트용 DTO
    public static RecipeDto updateDto(Long id, String title, String description, List<RecipeCategoryDto> categories, String status, String visibility, List<RecipeStepDto> steps) {
        return RecipeDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .categories(categories != null ? categories : new java.util.ArrayList<>())
                .status(status)
                .visibility(visibility)
                .steps(steps)
                .build();
    }
}
