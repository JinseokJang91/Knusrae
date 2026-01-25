package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.TrendingCategoryDto;
import com.knusrae.cook.api.domain.entity.Recipe;
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
    
    /**
     * 트렌딩 카테고리 조회
     * @param limit 조회할 카테고리 개수
     * @param period 기간 (7d, 30d)
     * @return 트렌딩 카테고리 목록
     */
    public List<TrendingCategoryDto> getTrendingCategories(int limit, String period) {
        log.debug("Fetching trending categories: limit={}, period={}", limit, period);
        
        // 기간 계산
        LocalDateTime since = switch (period) {
            case "7d" -> LocalDateTime.now().minusDays(7);
            case "30d" -> LocalDateTime.now().minusDays(30);
            default -> LocalDateTime.now().minusDays(30);
        };
        
        // Native Query로 카테고리별 레시피 수와 조회수 집계
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
                    .reason("TRENDING") // 기본값: 인기 급상승
                    .build();
            
            trendingCategories.add(dto);
        }
        
        log.info("Retrieved {} trending categories", trendingCategories.size());
        return trendingCategories;
    }
    
    /**
     * 카테고리별 레시피 조회
     * @param codeId 코드 ID
     * @param detailCodeId 상세 코드 ID
     * @param limit 조회할 레시피 개수
     * @param sortBy 정렬 기준 (latest, popular, mixed)
     * @return 레시피 목록
     */
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
        
        // 정렬 기준 설정
        String orderBy = switch (sortBy) {
            case "latest" -> "r.created_at DESC";
            case "popular" -> "r.hits DESC";
            case "mixed" -> "r.hits DESC, r.created_at DESC"; // 조회수와 최신순 혼합
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
    
    /**
     * 카테고리 정보 조회
     * @param codeId 코드 ID
     * @param detailCodeId 상세 코드 ID
     * @return 카테고리 정보 (간단)
     */
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
