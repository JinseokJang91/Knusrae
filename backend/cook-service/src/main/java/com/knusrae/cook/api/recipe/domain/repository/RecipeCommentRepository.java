package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeCommentRepository extends JpaRepository<RecipeComment, Long> {
    // 특정 레시피의 모든 댓글 조회 (최신순)
    List<RecipeComment> findAllByRecipeOrderByCreatedAtDesc(Recipe recipe);
    
    // 특정 레시피의 최상위 댓글만 조회 (parentId가 null인 댓글) - Pagination 지원
    Page<RecipeComment> findAllByRecipeAndParentIdIsNullOrderByCreatedAtDesc(Recipe recipe, Pageable pageable);
    
    // 특정 댓글의 대댓글 조회
    List<RecipeComment> findAllByParentIdOrderByCreatedAtAsc(Long parentId);
    
    // 특정 레시피의 댓글 개수(대댓글 제외)
    @Query("SELECT count(rc.id) FROM RecipeComment rc WHERE rc.recipe = :recipe AND rc.parentId IS NULL")
    long countByRecipe(Recipe recipe);
    
    // 여러 레시피의 댓글 개수를 한 번에 조회 (N+1 문제 해결)
    @Query("SELECT rc.recipe.id, count(rc.id) FROM RecipeComment rc WHERE rc.recipe IN :recipes AND rc.parentId IS NULL GROUP BY rc.recipe.id")
    List<Object[]> countByRecipes(List<Recipe> recipes);
    
    // 특정 사용자가 작성한 댓글 조회
    List<RecipeComment> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    
    // 특정 레시피의 댓글 개수 조회 (recipeId 기준)
    long countByRecipeId(Long recipeId);
}
