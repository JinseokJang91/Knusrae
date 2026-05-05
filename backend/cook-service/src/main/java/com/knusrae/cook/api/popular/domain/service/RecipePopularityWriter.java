package com.knusrae.cook.api.popular.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipePopularity;
import com.knusrae.cook.api.recipe.domain.repository.RecipePopularityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 레시피 인기도 저장 전용 서비스.
 * REQUIRES_NEW 트랜잭션으로 각 레시피 처리를 독립적으로 격리하여
 * 한 레시피의 실패가 전체 배치를 롤백시키지 않도록 한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipePopularityWriter {

    private final RecipePopularityRepository recipePopularityRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePopularity(Recipe recipe, LocalDateTime now,
                               long hits24h, long hits7d, long hits30d,
                               long favoriteCount, long commentCount,
                               long favoriteIncrease24h, double popularityScore) {
        Long recipeId = recipe.getId();

        RecipePopularity popularity = recipePopularityRepository
                .findById(recipeId)
                .orElseGet(() -> RecipePopularity.builder()
                        .recipeId(recipeId)
                        .recipe(recipe)
                        .calculatedAt(now)
                        .build());

        popularity.updatePopularity(
                hits24h, hits7d, hits30d,
                favoriteCount, commentCount,
                favoriteIncrease24h, popularityScore
        );

        recipePopularityRepository.save(popularity);
    }
}
