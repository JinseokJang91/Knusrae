package com.knusrae.cook.api.category.domain.service;

import com.knusrae.cook.api.category.dto.TrendingCategoryDto;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.dto.RecipeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {
    private final EntityManager entityManager;

    public List<TrendingCategoryDto> getTrendingCategories(int limit, String period) {
        log.debug("Fetching trending categories: limit={}, period={}", limit, period);
        LocalDateTime since = switch (period) {
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            default -> LocalDateTime.now().minusDays(30);
        };
        String sql = """
            SELECT 
                rc.code_id,
                rc.detail_code_id,
                cc.code_name,
                ccd.code_name as detail_name,
                COUNT(DISTINCT r.id) as recipe_count,
                COALESCE(SUM(r.hits), 0) as total_hits
            FROM recipe_category rc
            INNER JOIN recipe r ON rc.recipe_id = r.id
            INNER JOIN common_code cc ON rc.code_id = cc.code_id
            INNER JOIN common_code_detail ccd ON rc.code_id = ccd.code_id 
                AND rc.detail_code_id = ccd.detail_code_id
            WHERE rc.code_group = 'CATEGORY'
                AND r.status = 'PUBLISHED'
                AND r.visibility = 'PUBLIC'
                AND r.created_at >= :since
            GROUP BY rc.code_id, rc.detail_code_id, cc.code_name, ccd.code_name
            ORDER BY total_hits DESC, recipe_count DESC
            LIMIT :limit
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("since", since);
        query.setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<TrendingCategoryDto> trendingCategories = new ArrayList<>();
        for (Object[] row : results) {
            TrendingCategoryDto dto = TrendingCategoryDto.builder()
                    .codeId((String) row[0])
                    .detailCodeId((String) row[1])
                    .codeName((String) row[2])
                    .detailName((String) row[3])
                    .recipeCount(((Number) row[4]).longValue())
                    .totalHits(((Number) row[5]).longValue())
                    .reason("TRENDING")
                    .build();
            trendingCategories.add(dto);
        }
        log.info("Retrieved {} trending categories", trendingCategories.size());
        return trendingCategories;
    }

    public List<RecipeDto> getCategoryRecipes(String codeId, String detailCodeId, int limit, String sortBy) {
        log.debug("Fetching recipes for category: codeId={}, detailCodeId={}, limit={}, sortBy={}",
                codeId, detailCodeId, limit, sortBy);
        String sql = """
            SELECT DISTINCT r.*
            FROM recipe r
            INNER JOIN recipe_category rc ON r.id = rc.recipe_id
            WHERE rc.code_id = :codeId
                AND rc.detail_code_id = :detailCodeId
                AND r.status = 'PUBLISHED'
                AND r.visibility = 'PUBLIC'
            ORDER BY %s
            LIMIT :limit
            """;
        String orderBy = switch (sortBy) {
            case "latest" -> "r.created_at DESC";
            case "popular" -> "r.hits DESC";
            case "mixed" -> "r.hits DESC, r.created_at DESC";
            default -> "r.hits DESC, r.created_at DESC";
        };
        sql = String.format(sql, orderBy);
        Query query = entityManager.createNativeQuery(sql, Recipe.class);
        query.setParameter("codeId", codeId);
        query.setParameter("detailCodeId", detailCodeId);
        query.setParameter("limit", limit);
        @SuppressWarnings("unchecked")
        List<Recipe> recipes = query.getResultList();
        List<RecipeDto> recipeDtos = recipes.stream()
                .map(RecipeDto::from)
                .toList();
        log.info("Retrieved {} recipes for category {}:{}", recipeDtos.size(), codeId, detailCodeId);
        return recipeDtos;
    }

    public TrendingCategoryDto getCategoryInfo(String codeId, String detailCodeId) {
        log.debug("Fetching category info: codeId={}, detailCodeId={}", codeId, detailCodeId);
        String sql = """
            SELECT 
                cc.code_id,
                ccd.detail_code_id,
                cc.code_name,
                ccd.code_name as detail_name,
                COUNT(DISTINCT rc.recipe_id) as recipe_count
            FROM common_code cc
            INNER JOIN common_code_detail ccd ON cc.code_id = ccd.code_id
            LEFT JOIN recipe_category rc ON ccd.code_id = rc.code_id 
                AND ccd.detail_code_id = rc.detail_code_id
            WHERE cc.code_id = :codeId
                AND ccd.detail_code_id = :detailCodeId
            GROUP BY cc.code_id, ccd.detail_code_id, cc.code_name, ccd.code_name
            """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("codeId", codeId);
        query.setParameter("detailCodeId", detailCodeId);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        if (results.isEmpty()) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + codeId + ":" + detailCodeId);
        }
        Object[] row = results.get(0);
        return TrendingCategoryDto.builder()
                .codeId((String) row[0])
                .detailCodeId((String) row[1])
                .codeName((String) row[2])
                .detailName((String) row[3])
                .recipeCount(((Number) row[4]).longValue())
                .totalHits(0L)
                .reason("CATEGORY")
                .build();
    }
}
