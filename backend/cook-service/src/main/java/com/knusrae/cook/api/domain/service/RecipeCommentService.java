package com.knusrae.cook.api.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeComment;
import com.knusrae.cook.api.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.dto.RecipeCommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeCommentService {
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 생성
     */
    @Transactional
    public RecipeCommentDto createComment(Long recipeId, Long memberId, String content, Long parentId) {
        // 레시피 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // parentId가 있는 경우 (대댓글), 부모 댓글이 존재하는지 확인
        if (parentId != null) {
            RecipeComment parentComment = recipeCommentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with id: " + parentId));
            
            // 대댓글의 대댓글은 허용하지 않음 (1-depth만 허용)
            if (parentComment.getParentId() != null) {
                throw new IllegalArgumentException("대댓글의 대댓글은 작성할 수 없습니다.");
            }
        }

        // 댓글 생성
        RecipeComment comment = RecipeComment.builder()
                .recipe(recipe)
                .memberId(memberId)
                .content(content)
                .parentId(parentId)
                .build();

        RecipeComment savedComment = recipeCommentRepository.save(comment);

        // Member 정보 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;

        return RecipeCommentDto.fromEntity(savedComment, memberName, memberNickname);
    }

    /**
     * 특정 레시피의 모든 댓글 조회 (대댓글 포함, 계층 구조)
     */
    public List<RecipeCommentDto> getCommentsByRecipeId(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        // 모든 댓글 조회
        List<RecipeComment> allComments = recipeCommentRepository.findAllByRecipeOrderByCreatedAtDesc(recipe);

        // Member 정보 일괄 조회
        List<Long> memberIds = allComments.stream()
                .map(RecipeComment::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // 댓글을 DTO로 변환
        Map<Long, RecipeCommentDto> commentMap = allComments.stream()
                .collect(Collectors.toMap(
                        RecipeComment::getId,
                        comment -> {
                            Member member = memberMap.get(comment.getMemberId());
                            String memberName = member != null ? member.getName() : "사용자";
                            String memberNickname = member != null ? member.getNickname() : null;
                            return RecipeCommentDto.fromEntity(comment, memberName, memberNickname);
                        }
                ));

        // 최상위 댓글과 대댓글 분리
        List<RecipeCommentDto> topLevelComments = new ArrayList<>();
        
        for (RecipeCommentDto commentDto : commentMap.values()) {
            if (commentDto.getParentId() == null) {
                // 최상위 댓글인 경우
                commentDto.setReplies(new ArrayList<>());
                topLevelComments.add(commentDto);
            } else {
                // 대댓글인 경우, 부모 댓글에 추가
                RecipeCommentDto parentComment = commentMap.get(commentDto.getParentId());
                if (parentComment != null) {
                    if (parentComment.getReplies() == null) {
                        parentComment.setReplies(new ArrayList<>());
                    }
                    parentComment.getReplies().add(commentDto);
                }
            }
        }

        // 대댓글 정렬 (오래된 순)
        for (RecipeCommentDto comment : topLevelComments) {
            if (comment.getReplies() != null) {
                comment.getReplies().sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
            }
        }

        // 최상위 댓글 정렬 (최신순)
        topLevelComments.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return topLevelComments;
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public RecipeCommentDto updateComment(Long commentId, Long memberId, String content) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }

        comment.updateContent(content);

        // Member 정보 조회
        Member member = memberRepository.findById(memberId).orElse(null);
        String memberName = member != null ? member.getName() : "사용자";
        String memberNickname = member != null ? member.getNickname() : null;

        return RecipeCommentDto.fromEntity(comment, memberName, memberNickname);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        RecipeComment comment = recipeCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));

        // 작성자 확인
        if (!comment.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }

        // 대댓글이 있는 경우에도 삭제 (cascade)
        if (comment.getParentId() == null) {
            // 최상위 댓글인 경우, 해당 댓글의 대댓글도 모두 삭제
            List<RecipeComment> replies = recipeCommentRepository.findAllByParentIdOrderByCreatedAtAsc(commentId);
            recipeCommentRepository.deleteAll(replies);
        }

        recipeCommentRepository.delete(comment);
    }

    /**
     * 특정 레시피의 댓글 개수 조회
     */
    public long getCommentCount(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        return recipeCommentRepository.countByRecipe(recipe);
    }
}

