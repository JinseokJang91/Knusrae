package com.knusrae.cook.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecipeDto {
    private Long id;

    @NotBlank(message = "레시피 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "레시피 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    private String status;
    private String visibility;
    private String thumbnail;

    private Long hits;

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    private List<RecipeStepDto> steps;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecipeDto(Recipe recipe) {
        this.id = recipe.getId();
        this.title = recipe.getTitle();
        this.category = recipe.getCategory();
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
    }

    @Builder
    public RecipeDto(Long id, String title, String description, String category, Long hits, String status, String visibility, String thumbnail, Long memberId, List<RecipeStepDto> steps, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.hits = hits;
        this.status = status;
        this.visibility = visibility;
        this.thumbnail = thumbnail;
        this.memberId = memberId;
        this.steps = steps;
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
                .thumbnail(thumbnail)
                .hits(hits != null ? hits : 0L)
                .memberId(memberId)
                .build();
    }

    // 생성용 DTO
    public static RecipeDto createDto(String title, String description, String category, String thumbnail, Long memberId, List<RecipeStepDto> steps) {
        return RecipeDto.builder()
                .title(title)
                .description(description)
                .category(category)
                .status(Status.DRAFT.name())
                .visibility(Visibility.PUBLIC.name())
                .thumbnail(thumbnail)
                .memberId(memberId)
                .steps(steps)
                .build();
    }

    // 업데이트용 DTO
    public static RecipeDto updateDto(Long id, String title, String description, String category, String thumbnail, String status, String visibility, List<RecipeStepDto> steps) {
        return RecipeDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .category(category)
                .status(status)
                .visibility(visibility)
                .thumbnail(thumbnail)
                .steps(steps)
                .build();
    }
}
