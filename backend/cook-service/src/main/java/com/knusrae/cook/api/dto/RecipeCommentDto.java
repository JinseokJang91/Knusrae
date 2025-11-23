package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecipeComment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeCommentDto {
    private Long id;
    private Long parentId;
    private String content;
    private Long memberId;
    private String memberName;
    private String memberNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipeCommentDto> replies;

    public static RecipeCommentDto fromEntity(RecipeComment recipeComment, String memberName, String memberNickname) {
        return RecipeCommentDto.builder()
                .id(recipeComment.getId())
                .parentId(recipeComment.getParentId())
                .content(recipeComment.getContent())
                .memberId(recipeComment.getMemberId())
                .memberName(memberName)
                .memberNickname(memberNickname)
                .createdAt(recipeComment.getCreatedAt())
                .updatedAt(recipeComment.getUpdatedAt())
                .build();
    }
    
    public static RecipeCommentDto fromEntity(RecipeComment recipeComment) {
        return fromEntity(recipeComment, "댓글 작성자", null);
    }
}
