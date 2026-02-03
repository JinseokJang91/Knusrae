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
        Long recipeId = recipe.getId();
        if (recipeId == null) {
            log.warn("Recipe ID is null, skipping popularity calculation");
            return;
        }
        
        // 1. 최근 24시간 조회수
        long hits24h = recipeViewRepository.countByRecipeIdAndViewedAtAfter(recipeId, oneDayAgo);
        
        // 2. 최근 7일 조회수
        long hits7d = recipeViewRepository.countByRecipeIdAndViewedAtAfter(recipeId, sevenDaysAgo);
        
        // 3. 전체 찜 개수
        long favoriteCount = recipeFavoriteRepository.countByRecipeId(recipeId);
        
        // 4. 댓글 개수
        long commentCount = recipeCommentRepository.countByRecipeId(recipeId);
        
        // 5. 최근 24시간 찜 증가량 (특정 레시피의 찜 증가량)
        long favoriteIncrease24h = recipeFavoriteRepository
                .countByRecipeIdAndCreatedAtAfter(recipeId, oneDayAgo);
        
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

