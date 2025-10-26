package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private String content;
    private Double rating;
    private Long memberId;
    private String memberName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .content(review.getContent())
                .rating(review.getScore().doubleValue())
                .memberId(review.getMemberId())
                .memberName("리뷰 작성자") // TODO: 실제 사용자 이름 조회
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}