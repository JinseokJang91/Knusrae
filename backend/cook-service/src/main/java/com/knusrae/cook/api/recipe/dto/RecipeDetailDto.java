package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeDetailDto {
    private Long id;
    private String title;
    private String introduction;
    private List<RecipeCategoryDto> categories;
    private List<RecipeCookingTipDto> cookingTips;
    private List<RecipeIngredientGroupDto> ingredientGroups;
    private String status;
    private String visibility;
    private String thumbnail;
    private Long hits;
    private Long memberId;
    private String memberName;
    private String memberNickname;
    private String memberProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipeStepDetailDto> steps;
    private List<RecipeImageDto> images;
    private List<RecipeCommentDto> comments;
    private RecipeStatsDto stats;

    public static RecipeDetailDto fromEntity(Recipe recipe) {
        return fromEntity(recipe, "작성자");
    }

    public static RecipeDetailDto fromEntity(Recipe recipe, String memberName) {
        return RecipeDetailDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .introduction(recipe.getDescription())
                .categories(recipe.getRecipeCategories().stream()
                        .map(RecipeCategoryDto::fromEntity)
                        .collect(Collectors.toList()))
                .cookingTips(recipe.getRecipeCookingTips().stream()
                        .map(RecipeCookingTipDto::fromEntity)
                        .collect(Collectors.toList()))
                .ingredientGroups(recipe.getRecipeIngredientGroups().stream()
                        .map(RecipeIngredientGroupDto::fromEntity)
                        .collect(Collectors.toList()))
                .status(recipe.getStatus().name())
                .visibility(recipe.getVisibility().name())
                .thumbnail(recipe.getThumbnail())
                .hits(recipe.getHits())
                .memberId(recipe.getMemberId())
                .memberName(memberName)
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .steps(recipe.getRecipeDetails().stream()
                        .map(RecipeStepDetailDto::fromEntity)
                        .collect(Collectors.toList()))
                .images(recipe.getRecipeImages().stream()
                        .map(RecipeImageDto::fromEntity)
                        .collect(Collectors.toList()))
                .comments(recipe.getRecipeComments().stream()
                        .map(RecipeCommentDto::fromEntity)
                        .collect(Collectors.toList()))
                .stats(RecipeStatsDto.builder()
                        .totalComments(recipe.getRecipeComments().size())
                        .totalReviews(0)
                        .averageRating(0.0)
                        .build())
                .build();
    }

    public void setMemberNickname(String memberNickname) {
        this.memberNickname = memberNickname;
    }

    public void setMemberProfileImage(String memberProfileImage) {
        this.memberProfileImage = memberProfileImage;
    }

    public void setStats(RecipeStatsDto stats) {
        this.stats = stats;
    }
}
