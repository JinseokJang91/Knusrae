package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeView;
import com.knusrae.cook.api.domain.repository.RecipeFavoriteRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.domain.repository.RecipeViewRepository;
import com.knusrae.cook.api.dto.RecommendedRecipeDto;
import com.knusrae.cook.api.dto.TodayRecommendationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecommendationService {
    
    private final RecipeViewRepository recipeViewRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;
    
    /**
     * 오늘의 레시피 추천
     * 
     * @param memberId 회원 ID (null이면 비로그인 사용자)
     * @param limit 반환할 레시피 개수
     * @return 추천 레시피 목록
     */
    public TodayRecommendationDto getTodayRecommendations(Long memberId, int limit) {
        if (memberId != null) {
            // 로그인 사용자 - 개인화 추천
            return getPersonalizedRecommendations(memberId, limit);
        } else {
            // 비로그인 사용자 - 일반 추천
            return getGeneralRecommendations(limit);
        }
    }
    
    /**
     * 개인화 추천 (로그인 사용자)
     */
    private TodayRecommendationDto getPersonalizedRecommendations(Long memberId, int limit) {
        log.info("Getting personalized recommendations for member: {}", memberId);
        
        // 1. 최근 30일 조회 카테고리 분석
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> recentCategories = recipeViewRepository
                .findRecentCategoriesByMember(memberId, since, PageRequest.of(0, 3));
        
        log.info("Recent categories: {}", recentCategories.size());
        
        // 2. 최근 7일 이내 본 레시피 ID 목록 (제외용)
        LocalDateTime recentlyViewed = LocalDateTime.now().minusDays(7);
        List<RecipeView> recentViews = recipeViewRepository
                .findByMemberIdOrderByViewedAtDesc(memberId, PageRequest.of(0, 100));
        Set<Long> excludedRecipeIds = recentViews.stream()
                .filter(view -> view.getViewedAt().isAfter(recentlyViewed))
                .map(RecipeView::getRecipeId)
                .collect(Collectors.toSet());
        
        log.info("Excluded recipes (viewed in last 7 days): {}", excludedRecipeIds.size());
        
        // 3. 후보 레시피 수집
        List<ScoredRecipe> candidates = new ArrayList<>();
        
        // 3-1. 최근 본 카테고리와 일치하는 레시피
        if (!recentCategories.isEmpty()) {
            for (Object[] row : recentCategories) {
                String categoryCode = row[0].toString();
                
                // 해당 카테고리의 최근 30일 레시피 조회
                List<Recipe> categoryRecipes = recipeRepository
                        .findRecentRecipesByCategory(
                                "CATEGORY",
                                categoryCode,
                                LocalDateTime.now().minusDays(30),
                                PageRequest.of(0, 10)
                        );
                
                for (Recipe recipe : categoryRecipes) {
                    if (!excludedRecipeIds.contains(recipe.getId())) {
                        candidates.add(new ScoredRecipe(
                                recipe,
                                40.0,
                                "최근 본 카테고리와 일치"
                        ));
                    }
                }
            }
        }
        
        // 4. 후보가 부족하면 일반 인기 레시피 추가
        if (candidates.size() < limit * 2) {
            List<Recipe> popularRecipes = recipeRepository
                    .findRecentPopularRecipes(PageRequest.of(0, 20));
            
            for (Recipe recipe : popularRecipes) {
                if (!excludedRecipeIds.contains(recipe.getId())) {
                    candidates.add(new ScoredRecipe(
                            recipe,
                            20.0,
                            "인기 레시피"
                    ));
                }
            }
        }
        
        log.info("Total candidates: {}", candidates.size());
        
        // 5. 점수 기반 정렬 및 상위 N개 선택
        List<RecommendedRecipeDto> recommendations = candidates.stream()
                .sorted(Comparator.comparingDouble(ScoredRecipe::getScore).reversed())
                .limit(limit)
                .map(scored -> {
                    int favoriteCount = (int) recipeFavoriteRepository.countByRecipeId(scored.getRecipe().getId());
                    return RecommendedRecipeDto.from(
                            scored.getRecipe(),
                            scored.getReason(),
                            favoriteCount
                    );
                })
                .collect(Collectors.toList());
        
        log.info("Final recommendations: {}", recommendations.size());
        
        return TodayRecommendationDto.builder()
                .recipes(recommendations)
                .recommendationType("PERSONALIZED")
                .refreshable(true)
                .build();
    }
    
    /**
     * 일반 추천 (비로그인 사용자)
     */
    private TodayRecommendationDto getGeneralRecommendations(int limit) {
        log.info("Getting general recommendations");
        
        // 최근 7일 인기 레시피 중 무작위 선택
        List<Recipe> popularRecipes = recipeRepository
                .findRecentPopularRecipes(PageRequest.of(0, limit * 3));
        
        // 무작위로 섞기
        Collections.shuffle(popularRecipes);
        
        // 상위 N개 선택
        List<RecommendedRecipeDto> recommendations = popularRecipes.stream()
                .limit(limit)
                .map(recipe -> {
                    int favoriteCount = (int) recipeFavoriteRepository.countByRecipeId(recipe.getId());
                    return RecommendedRecipeDto.from(
                            recipe,
                            "인기 레시피",
                            favoriteCount
                    );
                })
                .collect(Collectors.toList());
        
        return TodayRecommendationDto.builder()
                .recipes(recommendations)
                .recommendationType("GENERAL")
                .refreshable(true)
                .build();
    }
    
    /**
     * 점수가 매겨진 레시피 (내부 클래스)
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ScoredRecipe {
        private Recipe recipe;
        private double score;
        private String reason;
    }
}
