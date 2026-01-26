package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
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
    
    /**
     * 특정 카테고리의 최근 레시피 조회
     * 
     * @param codeGroup 코드 그룹 (예: CATEGORY)
     * @param detailCodeId 상세 코드 ID
     * @param since 이 시간 이후 생성된 레시피만
     * @param pageable 페이징 정보
     * @return 카테고리에 속한 최근 레시피 목록
     */
    List<Recipe> findRecentRecipesByCategory(String codeGroup, String detailCodeId, LocalDateTime since, Pageable pageable);
    
    /**
     * 최근 인기 레시피 조회 (조회수 기준)
     * 
     * @param pageable 페이징 정보
     * @return 조회수가 높은 레시피 목록
     */
    List<Recipe> findRecentPopularRecipes(Pageable pageable);
}
