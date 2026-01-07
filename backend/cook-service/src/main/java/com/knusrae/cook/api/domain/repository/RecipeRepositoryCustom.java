package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;

import java.util.List;

public interface RecipeRepositoryCustom {
    List<Recipe> findPublishedPublicRecipes();

    List<Recipe> findMemberRecipes(Long memberId);

    /**
     * 제목으로 레시피 검색 (공개된 레시피만)
     * 
     * @param keyword 검색어
     * @return 제목에 검색어가 포함된 공개 레시피 목록
     */
    List<Recipe> searchRecipesByTitle(String keyword);
}
