package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipePopularity;
import com.knusrae.cook.api.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PopularityCalculationService {
    
    private final RecipeRepository recipeRepository;
    private final RecipePopularityRepository recipePopularityRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;
    private final RecipeCommentRepository recipeCommentRepository;
    
    /**
     * 모든 레시피의 인기도 점수 계산
     */
    @Transactional
    public void calculateAllPopularityScores() {
        log.info("Starting popularity score calculation for all recipes");
        
        List<Recipe> allRecipes = recipeRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        int processedCount = 0;
        
        for (Recipe recipe : allRecipes) {
            try {
                calculateAndSavePopularity(recipe, now, oneDayAgo, sevenDaysAgo);
                processedCount++;
            } catch (Exception e) {
                log.error("Error calculating popularity for recipe {}: {}", recipe.getId(), e.getMessage());
            }
        }
        
        log.info("Popularity score calculation completed. Processed {} recipes", processedCount);
    }
    
    /**
     * 특정 레시피의 인기도 점수 계산
     */
    @Transactional
    public void calculateRecipePopularity(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다: " + recipeId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        calculateAndSavePopularity(recipe, now, oneDayAgo, sevenDaysAgo);
    }
    
    /**
     * 인기도 계산 및 저장
     */
    private void calculateAndSavePopularity(Recipe recipe, LocalDateTime now, 
                                           LocalDateTime oneDayAgo, LocalDateTime sevenDaysAgo) {
        // 1. 최근 24시간 조회수 (RecipeView가 없으면 0)
        long hits24h = 0L;
        
        // 2. 최근 7일 조회수 (RecipeView가 없으면 0)
        long hits7d = 0L;
        
        // 3. 전체 찜 개수
        Long recipeId = recipe.getId();
        if (recipeId == null) {
            log.warn("Recipe ID is null, skipping popularity calculation");
            return;
        }
        
        long favoriteCount = recipeFavoriteRepository.countByRecipeId(recipeId);
        
        // 4. 댓글 개수
        long commentCount = recipeCommentRepository.countByRecipeId(recipeId);
        
        // 5. 최근 24시간 찜 증가량
        long favoriteIncrease24h = recipeFavoriteRepository
                .findByMemberId(recipe.getMemberId()) // 임시로 모든 찜 조회
                .stream()
                .filter(fav -> fav.getCreatedAt().isAfter(oneDayAgo))
                .count();
        
        // 6. 레시피 게시일로부터 경과 일수
        long daysSinceCreated = ChronoUnit.DAYS.between(
                recipe.getCreatedAt().toLocalDate(),
                LocalDate.now()
        );
        
        // 7. 인기도 점수 계산
        double rawScore = 
                hits24h * 5.0 +
                hits7d * 3.0 +
                favoriteCount * 10.0 +
                commentCount * 8.0 +
                favoriteIncrease24h * 15.0;
        
        double popularityScore = rawScore / (daysSinceCreated + 1.0);
        
        // 8. RecipePopularity 엔티티에 저장 또는 업데이트
        RecipePopularity popularity = recipePopularityRepository
                .findById(recipeId)
                .orElse(RecipePopularity.builder()
                        .recipeId(recipeId)
                        .recipe(recipe)
                        .calculatedAt(now)
                        .build());
        
        popularity.updatePopularity(
                hits24h,
                hits7d,
                favoriteCount,
                commentCount,
                favoriteIncrease24h,
                popularityScore
        );
        
        recipePopularityRepository.save(popularity);
    }
    
    /**
     * 인기도 점수 재계산 (배치 작업용)
     */
    @Transactional
    public void recalculateAllScores() {
        log.info("Recalculating all popularity scores");
        calculateAllPopularityScores();
    }
}

