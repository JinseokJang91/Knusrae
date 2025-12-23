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
    private String imageUrl;
    private Long memberId;
    private String memberName;
    private String memberNickname;
    private String memberProfileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RecipeCommentDto> replies;
    
    // 부모 댓글 작성자 정보 (답글 작성 시 표시용)
    private Long parentMemberId;
    private String parentMemberNickname;

    public static RecipeCommentDto fromEntity(RecipeComment recipeComment, String memberName, String memberNickname, String memberProfileImage) {
        return RecipeCommentDto.builder()
                .id(recipeComment.getId())
                .parentId(recipeComment.getParentId())
                .content(recipeComment.getContent())
                .imageUrl(recipeComment.getImageUrl())
                .memberId(recipeComment.getMemberId())
                .memberName(memberName)
                .memberNickname(memberNickname)
                .memberProfileImage(memberProfileImage)
                .createdAt(recipeComment.getCreatedAt())
                .updatedAt(recipeComment.getUpdatedAt())
                .build();
    }
    
    public static RecipeCommentDto fromEntity(RecipeComment recipeComment, String memberName, String memberNickname) {
        return fromEntity(recipeComment, memberName, memberNickname, null);
    }
    
    public static RecipeCommentDto fromEntity(RecipeComment recipeComment) {
        return fromEntity(recipeComment, "댓글 작성자", null);
    }
}
