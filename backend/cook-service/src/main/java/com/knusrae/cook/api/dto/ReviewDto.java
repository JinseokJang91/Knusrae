package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.Review;
import com.knusrae.cook.api.domain.ReviewComment;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String images;

        @NotNull(message = "회원 ID는 필수입니다.")
        private Long memberId;

        @NotNull(message = "레시피 ID는 필수입니다.")
        private Long recipeId;

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Long score;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String images;

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        private Long score;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String images;
        private Long memberId;
        private Long recipeId;
        private Long score;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<ReviewCommentDto.Response> comments;

        public static Response from(Review review) {
            return Response.builder()
                    .id(review.getId())
                    .title(review.getTitle())
                    .content(review.getContent())
                    .images(review.getImages())
                    .memberId(review.getMemberId())
                    .recipeId(review.getRecipe().getId())
                    .score(review.getScore())
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .comments(review.getReviewComments().stream()
                            .map(ReviewCommentDto.Response::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private String title;
        private String content;
        private Long memberId;
        private Long recipeId;
        private String recipeTitle;
        private Long score;
        private LocalDateTime createdAt;
        private Long commentCount;

        public static ListResponse from(Review review) {
            return ListResponse.builder()
                    .id(review.getId())
                    .title(review.getTitle())
                    .content(review.getContent().length() > 100 ?
                            review.getContent().substring(0, 100) + "..." :
                            review.getContent())
                    .memberId(review.getMemberId())
                    .recipeId(review.getRecipe().getId())
                    .recipeTitle(review.getRecipe().getTitle())
                    .score(review.getScore())
                    .createdAt(review.getCreatedAt())
                    .commentCount((long) review.getReviewComments().size())
                    .build();
        }
    }
}

// ReviewCommentDto 클래스도 함께 정의
class ReviewCommentDto {
    @Getter
    @Setter
    public static class CreateRequest {
        private Long parentId;

        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;

        @NotNull(message = "회원 ID는 필수입니다.")
        private Long memberId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long parentId;
        private String content;
        private Long memberId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(ReviewComment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .parentId(comment.getParentId())
                    .content(comment.getContent())
                    .memberId(comment.getMemberId())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }
}