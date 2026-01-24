package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipePopularity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipePopularityRepository extends JpaRepository<RecipePopularity, Long> {
    
    /**
     * 인기도 순으로 레시피 조회
     */
    @Query("SELECT rp FROM RecipePopularity rp ORDER BY rp.popularityScore DESC")
    List<RecipePopularity> findTopByPopularityScore(Pageable pageable);
    
    /**
     * 24시간 조회수 순
     */
    @Query("SELECT rp FROM RecipePopularity rp ORDER BY rp.hits24h DESC")
    List<RecipePopularity> findTopByHits24h(Pageable pageable);
    
    /**
     * 7일 조회수 순
     */
    @Query("SELECT rp FROM RecipePopularity rp ORDER BY rp.hits7d DESC")
    List<RecipePopularity> findTopByHits7d(Pageable pageable);
}

