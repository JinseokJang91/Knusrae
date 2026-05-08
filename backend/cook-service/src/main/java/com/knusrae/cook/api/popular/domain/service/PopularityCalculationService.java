package com.knusrae.cook.api.popular.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipePopularity;
import com.knusrae.cook.api.recipe.domain.entity.RecipePopularityHistory;
import com.knusrae.cook.api.recipe.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PopularityCalculationService {
    
    private final RecipeRepository recipeRepository;
    private final RecipePopularityRepository recipePopularityRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeViewRepository recipeViewRepository;
    private final RecipePopularityHistoryRepository recipePopularityHistoryRepository;
    private final RecipePopularityWriter recipePopularityWriter;
    
    /**
     * 모든 레시피의 인기도 점수 계산.
     * 각 레시피를 REQUIRES_NEW 트랜잭션으로 독립 처리하여
     * 특정 레시피 실패가 전체 배치를 중단시키지 않도록 한다.
     */
    public void calculateAllPopularityScores() {
        log.info("Starting popularity score calculation for all recipes");
        
        List<Recipe> allRecipes = recipeRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        int processedCount = 0;
        
        for (Recipe recipe : allRecipes) {
            try {
                calculateAndSavePopularity(recipe, now, oneDayAgo, sevenDaysAgo, thirtyDaysAgo);
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
    public void calculateRecipePopularity(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다: " + recipeId));
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        calculateAndSavePopularity(recipe, now, oneDayAgo, sevenDaysAgo, thirtyDaysAgo);
    }
    
    /**
     * 인기도 지표 조회 후 저장 위임.
     * 저장은 RecipePopularityWriter(REQUIRES_NEW)에 위임하여 트랜잭션 격리.
     */
    private void calculateAndSavePopularity(Recipe recipe, LocalDateTime now,
                                            LocalDateTime oneDayAgo, LocalDateTime sevenDaysAgo,
                                            LocalDateTime thirtyDaysAgo) {
        Long recipeId = recipe.getId();
        if (recipeId == null) {
            log.warn("Recipe ID is null, skipping popularity calculation");
            return;
        }
        
        long hits24h = recipeViewRepository.countByRecipeIdAndViewedAtAfter(recipeId, oneDayAgo);
        long hits7d = recipeViewRepository.countByRecipeIdAndViewedAtAfter(recipeId, sevenDaysAgo);
        long hits30d = recipeViewRepository.countByRecipeIdAndViewedAtAfter(recipeId, thirtyDaysAgo);
        long favoriteCount = recipeFavoriteRepository.countByRecipeId(recipeId);
        long commentCount = recipeCommentRepository.countByRecipeId(recipeId);
        long favoriteIncrease24h = recipeFavoriteRepository
                .countByRecipeIdAndCreatedAtAfter(recipeId, oneDayAgo);
        
        long daysSinceCreated = ChronoUnit.DAYS.between(
                recipe.getCreatedAt().toLocalDate(),
                LocalDate.now()
        );
        
        double rawScore =
                hits24h * 5.0 +
                hits7d * 3.0 +
                favoriteCount * 10.0 +
                commentCount * 8.0 +
                favoriteIncrease24h * 15.0;
        
        double popularityScore = rawScore / (daysSinceCreated + 1.0);

        // 저장을 REQUIRES_NEW 트랜잭션으로 격리 (한 레시피 실패가 전체를 오염시키지 않도록)
        recipePopularityWriter.savePopularity(
                recipe, now,
                hits24h, hits7d, hits30d,
                favoriteCount, commentCount,
                favoriteIncrease24h, popularityScore
        );
    }
    
    /**
     * 인기도 점수 재계산 (배치 작업용)
     */
    @Transactional
    public void recalculateAllScores() {
        log.info("Recalculating all popularity scores");
        calculateAllPopularityScores();
    }
    
    /**
     * 순위 변동 추적 및 히스토리 저장
     * 이전 순위와 비교하여 트렌드 상태를 계산하고 히스토리를 저장합니다.
     */
    @Transactional
    public void trackRankingChanges() {
        log.info("Starting ranking change tracking");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime yesterdayStart = yesterday.minusHours(1);
        LocalDateTime yesterdayEnd = yesterday.plusHours(1);
        
        // 현재 순위 조회 (상위 100개)
        Pageable top100 = PageRequest.of(0, 100);
        List<RecipePopularity> currentRanking = recipePopularityRepository
                .findTopByPopularityScore(top100);
        
        log.info("Current ranking size: {}", currentRanking.size());
        
        // 이전 순위 조회 (24시간 전)
        List<RecipePopularityHistory> previousHistories = recipePopularityHistoryRepository
                .findByRecordedAtBetween(yesterdayStart, yesterdayEnd);
        
        // 레시피 ID -> 이전 순위 매핑
        Map<Long, Integer> previousRanks = previousHistories.stream()
                .collect(Collectors.toMap(
                        RecipePopularityHistory::getRecipeId,
                        RecipePopularityHistory::getRank,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
        
        log.info("Previous ranks found: {}", previousRanks.size());
        
        // 현재 순위를 히스토리에 저장
        int currentRank = 1;
        for (RecipePopularity popularity : currentRanking) {
            Long recipeId = popularity.getRecipeId();
            Integer previousRank = previousRanks.get(recipeId);
            
            // 히스토리 저장
            RecipePopularityHistory history = RecipePopularityHistory.builder()
                    .recipeId(recipeId)
                    .rank(currentRank)
                    .popularityScore(popularity.getPopularityScore())
                    .build();
            
            recipePopularityHistoryRepository.save(history);
            
            currentRank++;
        }
        
        log.info("Ranking change tracking completed. Saved {} history records", currentRanking.size());
    }
}

