package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipePopularityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecipePopularityHistoryRepository extends JpaRepository<RecipePopularityHistory, Long> {
    
    /**
     * 특정 시간 범위의 히스토리 조회
     */
    @Query("SELECT h FROM RecipePopularityHistory h WHERE h.recordedAt BETWEEN :start AND :end ORDER BY h.recordedAt DESC")
    List<RecipePopularityHistory> findByRecordedAtBetween(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );
    
    /**
     * 특정 레시피의 최근 히스토리 조회
     */
    @Query("SELECT h FROM RecipePopularityHistory h WHERE h.recipeId = :recipeId ORDER BY h.recordedAt DESC")
    List<RecipePopularityHistory> findByRecipeIdOrderByRecordedAtDesc(@Param("recipeId") Long recipeId);
    
    /**
     * 특정 시간의 순위 조회 (가장 가까운 시간)
     */
    @Query("""
        SELECT h FROM RecipePopularityHistory h 
        WHERE h.recordedAt <= :targetTime 
        AND h.recipeId = :recipeId
        ORDER BY h.recordedAt DESC
        """)
    List<RecipePopularityHistory> findByRecipeIdAndRecordedAtBeforeOrderByRecordedAtDesc(
            @Param("recipeId") Long recipeId, 
            @Param("targetTime") LocalDateTime targetTime
    );
}
