package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.Recipe;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedRecipeDto {
    private Long id;
    private String title;
    private String description;
    private String thumbnail;
    private Long memberId;
    private String memberNickname;
    private String memberProfileImage;
    private Long hits;
    private List<RecipeCategoryDto> categories;
    private Integer commentCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private String recommendReason; // 추천 이유
    
    public static RecommendedRecipeDto from(Recipe recipe, String recommendReason) {
        return RecommendedRecipeDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .thumbnail(recipe.getThumbnail())
                .memberId(recipe.getMemberId())
                .hits(recipe.getHits())
                .categories(recipe.getRecipeCategories().stream()
                        .map(RecipeCategoryDto::fromEntity)
                        .collect(Collectors.toList()))
                .commentCount(recipe.getRecipeComments().size())
                .createdAt(recipe.getCreatedAt())
                .recommendReason(recommendReason)
                .build();
    }
    
    public static RecommendedRecipeDto from(Recipe recipe, String recommendReason, Integer favoriteCount) {
        RecommendedRecipeDto dto = from(recipe, recommendReason);
        return RecommendedRecipeDto.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnail(dto.getThumbnail())
                .memberId(dto.getMemberId())
                .memberNickname(dto.getMemberNickname())
                .memberProfileImage(dto.getMemberProfileImage())
                .hits(dto.getHits())
                .categories(dto.getCategories())
                .commentCount(dto.getCommentCount())
                .favoriteCount(favoriteCount)
                .createdAt(dto.getCreatedAt())
                .recommendReason(recommendReason)
                .build();
    }
}
