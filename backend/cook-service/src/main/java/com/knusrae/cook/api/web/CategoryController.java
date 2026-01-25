package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.CategoryService;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.TrendingCategoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 트렌딩 카테고리 조회
     * 
     * @param limit 조회할 카테고리 개수 (기본 2)
     * @param period 기간 (7d, 30d) (기본 30d)
     * @return 트렌딩 카테고리 목록
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingCategories(
            @RequestParam(defaultValue = "2") int limit,
            @RequestParam(defaultValue = "30d") String period) {
        
        log.debug("GET /api/categories/trending - limit={}, period={}", limit, period);
        
        // limit 최대값 제한
        if (limit > 10) {
            limit = 10;
        }
        
        List<TrendingCategoryDto> categories = categoryService.getTrendingCategories(limit, period);
        
        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("period", period);
        
        log.info("Retrieved {} trending categories", categories.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 카테고리별 레시피 조회
     * 
     * @param codeId 코드 ID
     * @param detailCodeId 상세 코드 ID
     * @param limit 조회할 레시피 개수 (기본 12)
     * @param sort 정렬 기준 (latest, popular, mixed) (기본 mixed)
     * @return 카테고리 정보와 레시피 목록
     */
    @GetMapping("/{codeId}/{detailCodeId}/recipes")
    public ResponseEntity<Map<String, Object>> getCategoryRecipes(
            @PathVariable String codeId,
            @PathVariable String detailCodeId,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(defaultValue = "mixed") String sort) {
        
        log.debug("GET /api/categories/{}/{}/recipes - limit={}, sort={}", 
                codeId, detailCodeId, limit, sort);
        
        // limit 최대값 제한
        if (limit > 50) {
            limit = 50;
        }
        
        // 카테고리 정보 조회
        TrendingCategoryDto category = categoryService.getCategoryInfo(codeId, detailCodeId);
        
        // 레시피 목록 조회
        List<RecipeDto> recipes = categoryService.getCategoryRecipes(codeId, detailCodeId, limit, sort);
        
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("recipes", recipes);
        response.put("totalCount", recipes.size());
        
        log.info("Retrieved {} recipes for category {}:{}", recipes.size(), codeId, detailCodeId);
        return ResponseEntity.ok(response);
    }
}
