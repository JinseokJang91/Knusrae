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
public class RecipeSimpleDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private Long hits;
    private Long memberId;
    private String memberName;
    private String memberNickname;
    private String memberProfileImage;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipeCategoryDto> categories;
    private List<RecipeCookingTipDto> cookingTips;
    private Long favoriteCount;
    private Long commentCount;
    private Double averageRating;

    public static RecipeSimpleDto from(Recipe recipe) {
        return RecipeSimpleDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .thumbnail(recipe.getThumbnail())
                .hits(recipe.getHits())
                .memberId(recipe.getMemberId())
                .visibility(recipe.getVisibility().name())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .categories(recipe.getRecipeCategories().stream()
                        .map(RecipeCategoryDto::fromEntity)
                        .collect(Collectors.toList()))
                .cookingTips(recipe.getRecipeCookingTips().stream()
                        .map(RecipeCookingTipDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public static RecipeSimpleDto from(Recipe recipe, String memberName) {
        RecipeSimpleDto dto = from(recipe);
        return RecipeSimpleDto.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .hits(dto.getHits())
                .memberId(dto.getMemberId())
                .memberName(memberName)
                .visibility(dto.getVisibility())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .categories(dto.getCategories())
                .cookingTips(dto.getCookingTips())
                .build();
    }
}
