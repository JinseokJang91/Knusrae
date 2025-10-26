package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeCommentDto {
    private Long id;
    private String content;
    private Long memberId;
    private String memberName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RecipeCommentDto fromEntity(RecipeComment recipeComment) {
        return RecipeCommentDto.builder()
                .id(recipeComment.getId())
                .content(recipeComment.getContent())
                .memberId(recipeComment.getMemberId())
                .memberName("댓글 작성자") // TODO: 실제 사용자 이름 조회
                .createdAt(recipeComment.getCreatedAt())
                .updatedAt(recipeComment.getUpdatedAt())
                .build();
    }
}
