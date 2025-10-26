package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDetailDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String status;
    private String visibility;
    private String thumbnail;
    private Long hits;
    private Long memberId;
    private String memberName; // 작성자 이름
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 조리 단계
    private List<RecipeStepDetailDto> steps;
    
    // 이미지들
    private List<RecipeImageDto> images;
    
    // 댓글들
    private List<RecipeCommentDto> comments;
    
    // 리뷰들
    private List<ReviewDto> reviews;
    
    // 통계 정보
    private RecipeStatsDto stats;

    public static RecipeDetailDto fromEntity(Recipe recipe) {
        return RecipeDetailDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .category(recipe.getCategory())
                .status(recipe.getStatus().name())
                .visibility(recipe.getVisibility().name())
                .thumbnail(recipe.getThumbnail())
                .hits(recipe.getHits())
                .memberId(recipe.getMemberId())
                .memberName("작성자") // TODO: 실제 사용자 이름 조회
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
                .reviews(recipe.getReviews().stream()
                        .map(ReviewDto::fromEntity)
                        .collect(Collectors.toList()))
                .stats(RecipeStatsDto.builder()
                        .totalComments(recipe.getRecipeComments().size())
                        .totalReviews(recipe.getReviews().size())
                        .averageRating(recipe.getReviews().stream()
                                .mapToDouble(Review::getScore)
                                .average()
                                .orElse(0.0))
                        .build())
                .build();
    }
}
