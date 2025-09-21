package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.dto.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RecipeRepositoryCustom {
    // 카테고리별 조회
    List<Recipe> findAllByCategory(String category);

    // 사용자별 레시피 조회
    List<Recipe> findAllByUserId(Long userId);

    // 상태별 레시피 조회
    List<Recipe> findAllByVisibility(Visibility visibility);

    // 제목으로 검색 (LIKE 검색)
    List<Recipe> findByTitleContaining(String keyword);

    // 복합 조건 검색 (제목 + 카테고리)
    List<Recipe> findByTitleAndCategory(String keyword, String category);

    // 인기 레시피 조회 (조회수 높은 순)
    List<Recipe> findPopularRecipes(int limit);

    // 페이징 처리된 레시피 목록
    Page<Recipe> findRecipesWithPaging(String keyword, String category, Visibility visibility, Pageable pageable);

    // 사용자별 레시피 개수
    Long countByUserId(Long userId);

    // 카테고리별 레시피 개수
    Long countByCategory(String category);
}
