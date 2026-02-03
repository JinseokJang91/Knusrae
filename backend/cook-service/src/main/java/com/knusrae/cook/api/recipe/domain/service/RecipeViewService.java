package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeView;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeViewRepository;
import com.knusrae.cook.api.recipe.dto.RecipeSimpleDto;
import com.knusrae.cook.api.recipe.dto.RecipeViewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeViewService {
    private final RecipeViewRepository recipeViewRepository;
    private final RecipeRepository recipeRepository;

    @Transactional
    public Map<String, Object> createOrUpdateRecipeView(Long memberId, Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + recipeId));
        RecipeView recipeView = recipeViewRepository.findByMemberIdAndRecipeId(memberId, recipeId)
                .orElse(null);
        boolean isNew = false;
        if (recipeView == null) {
            recipeView = RecipeView.builder()
                    .memberId(memberId)
                    .recipeId(recipeId)
                    .viewedAt(LocalDateTime.now())
                    .build();
            isNew = true;
        } else {
            recipeView.updateViewedAt();
        }
        RecipeView savedView = recipeViewRepository.save(recipeView);
        if (isNew) {
            recipe.increaseHits();
            recipeRepository.save(recipe);
        }
        log.info("Recipe view {} for member: {}, recipe: {}", isNew ? "created" : "updated", memberId, recipeId);
        Map<String, Object> result = new HashMap<>();
        result.put("view", RecipeViewDto.from(savedView));
        result.put("isNew", isNew);
        return result;
    }

    public Map<String, Object> getRecentViews(Long memberId, int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        List<RecipeView> views = recipeViewRepository.findByMemberIdOrderByViewedAtDesc(memberId, pageable);
        List<Long> recipeIds = views.stream()
                .map(RecipeView::getRecipeId)
                .collect(Collectors.toList());
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);
        Map<Long, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));
        List<RecipeViewDto> viewDtos = views.stream()
                .map(view -> {
                    Recipe recipe = recipeMap.get(view.getRecipeId());
                    if (recipe != null) {
                        RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe);
                        return RecipeViewDto.from(view, recipeDto);
                    } else {
                        return RecipeViewDto.from(view);
                    }
                })
                .collect(Collectors.toList());
        long totalCount = recipeViewRepository.countByMemberId(memberId);
        Map<String, Object> result = new HashMap<>();
        result.put("views", viewDtos);
        result.put("totalCount", totalCount);
        log.info("Retrieved {} recent views for member: {} (total: {})", viewDtos.size(), memberId, totalCount);
        return result;
    }

    @Transactional
    public void deleteAllViewsByMember(Long memberId) {
        recipeViewRepository.deleteByMemberId(memberId);
        log.info("Deleted all views for member: {}", memberId);
    }

    public List<Object[]> getRecentCategories(Long memberId, int daysAgo, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysAgo);
        Pageable pageable = PageRequest.of(0, limit);
        return recipeViewRepository.findRecentCategoriesByMember(memberId, since, pageable);
    }
}
