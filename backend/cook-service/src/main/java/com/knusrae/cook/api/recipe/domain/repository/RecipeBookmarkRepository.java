package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeBookmarkRepository extends JpaRepository<RecipeBookmark, Long> {

    /**
     * 레시피북별 북마크 목록 조회 (최신순)
     */
    List<RecipeBookmark> findByRecipeBookIdOrderByCreatedAtDesc(Long recipeBookId);

    /**
     * 레시피북 내 특정 레시피 북마크 존재 여부 확인
     */
    boolean existsByRecipeBookIdAndRecipeId(Long recipeBookId, Long recipeId);

    /**
     * 레시피북 내 특정 레시피 북마크 조회
     */
    Optional<RecipeBookmark> findByRecipeBookIdAndRecipeId(Long recipeBookId, Long recipeId);

    /**
     * 레시피북 내 특정 레시피 북마크 삭제
     */
    void deleteByRecipeBookIdAndRecipeId(Long recipeBookId, Long recipeId);

    /**
     * 레시피북 내 북마크 개수 조회
     */
    long countByRecipeBookId(Long recipeBookId);

    /**
     * 특정 레시피의 북마크 목록 조회 (어떤 레시피북에 저장되었는지)
     */
    List<RecipeBookmark> findByRecipeIdAndMemberId(Long recipeId, Long memberId);

    /**
     * 회원의 특정 레시피 북마크 목록 조회
     */
    List<RecipeBookmark> findByMemberId(Long memberId);

    /**
     * 레시피북 삭제 시 관련 북마크 모두 삭제
     */
    void deleteByRecipeBookId(Long recipeBookId);

    /**
     * 레시피북별 북마크 개수 조회 (여러 레시피북)
     */
    @Query("SELECT rb.recipeBookId, COUNT(rb) FROM RecipeBookmark rb WHERE rb.recipeBookId IN :recipeBookIds GROUP BY rb.recipeBookId")
    List<Object[]> countByRecipeBookIds(@Param("recipeBookIds") List<Long> recipeBookIds);
}
