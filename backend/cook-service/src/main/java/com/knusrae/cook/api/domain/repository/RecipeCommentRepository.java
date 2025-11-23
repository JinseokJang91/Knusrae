package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {
    // 특정 레시피의 모든 댓글 조회 (최신순)
    List<RecipeComment> findAllByRecipeOrderByCreatedAtDesc(Recipe recipe);
    
    // 특정 레시피의 최상위 댓글만 조회 (parentId가 null인 댓글)
    List<RecipeComment> findAllByRecipeAndParentIdIsNullOrderByCreatedAtDesc(Recipe recipe);
    
    // 특정 댓글의 대댓글 조회
    List<RecipeComment> findAllByParentIdOrderByCreatedAtAsc(Long parentId);
    
    // 특정 레시피의 댓글 개수
    long countByRecipe(Recipe recipe);
    
    // 특정 사용자가 작성한 댓글 조회
    List<RecipeComment> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}

