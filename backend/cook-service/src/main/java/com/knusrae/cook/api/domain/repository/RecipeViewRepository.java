package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipeView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeViewRepository extends JpaRepository<RecipeView, Long> {
    
    /**
     * 특정 회원의 조회 기록 조회 (최신순)
     */
    @Query("SELECT rv FROM RecipeView rv WHERE rv.memberId = :memberId ORDER BY rv.viewedAt DESC")
    List<RecipeView> findByMemberIdOrderByViewedAtDesc(Long memberId, Pageable pageable);
    
    /**
     * 특정 회원과 레시피의 조회 기록 존재 여부
     */
    Optional<RecipeView> findByMemberIdAndRecipeId(Long memberId, Long recipeId);
    
    /**
     * 특정 레시피의 조회 기록 개수
     */
    long countByRecipeId(Long recipeId);
    
    /**
     * 특정 기간 동안의 조회 기록 개수
     */
    @Query("SELECT COUNT(rv) FROM RecipeView rv WHERE rv.recipeId = :recipeId AND rv.viewedAt >= :since")
    long countByRecipeIdAndViewedAtAfter(Long recipeId, LocalDateTime since);
    
    /**
     * 오래된 조회 기록 삭제 (회원당 최대 개수 유지)
     */
    @Modifying
    @Query(value = """
        DELETE FROM recipe_view 
        WHERE id IN (
            SELECT id FROM (
                SELECT id, ROW_NUMBER() OVER (PARTITION BY member_id ORDER BY viewed_at DESC) as rn
                FROM recipe_view
            ) tmp WHERE rn > :maxCount
        )
        """, nativeQuery = true)
    void deleteOldViewsByMember(int maxCount);
    
    /**
     * 특정 회원의 최근 본 카테고리 조회
     */
    @Query("""
        SELECT rc.detail.id, COUNT(rv) as cnt
        FROM RecipeView rv
        JOIN Recipe r ON rv.recipeId = r.id
        JOIN RecipeCategory rc ON rc.recipe.id = r.id
        WHERE rv.memberId = :memberId 
        AND rv.viewedAt >= :since
        AND rc.codeGroup = 'CATEGORY'
        GROUP BY rc.detail.id
        ORDER BY cnt DESC
        """)
    List<Object[]> findRecentCategoriesByMember(Long memberId, LocalDateTime since, Pageable pageable);
    
    /**
     * 특정 회원의 조회 기록 개수
     */
    long countByMemberId(Long memberId);
    
    /**
     * 특정 회원의 모든 조회 기록 삭제
     */
    void deleteByMemberId(Long memberId);
}
