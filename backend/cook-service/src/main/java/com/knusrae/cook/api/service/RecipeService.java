package com.knusrae.cook.api.service;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.Visibility;
import com.knusrae.cook.api.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {

    private final RecipeRepository recipeRepository;

    // CREATE - 레시피 생성
    @Transactional
    public RecipeDto createRecipe(RecipeDto recipeDto) {
        Recipe recipe = recipeDto.toEntity();
        Recipe savedRecipe = recipeRepository.save(recipe);
        return new RecipeDto(savedRecipe);
    }

    // READ - 전체 레시피 목록 조회
    public List<RecipeDto> getRecipeList() {
        List<Recipe> recipeList = recipeRepository.findAllByOrderByCreatedAtDesc();
        return recipeList.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // READ - 단일 레시피 조회 (조회수 증가)
    @Transactional
    public RecipeDto getRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        recipe.increaseHits(); // 조회수 증가
        return new RecipeDto(recipe);
    }

    // UPDATE - 레시피 수정
    @Transactional
    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        recipe.updateRecipe(recipeDto.getTitle(), recipeDto.getCategory(),
                Visibility.valueOf(recipeDto.getVisibility()));

        return new RecipeDto(recipe);
    }

    // DELETE - 레시피 삭제 (물리적 삭제)
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        recipeRepository.delete(recipe);
    }

    // DELETE - 레시피 비활성화 (논리적 삭제)
    @Transactional
    public void deactivateRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        recipe.changeVisibility(Visibility.PRIVATE);
    }

    // QueryDSL 기반 복합 조회 기능들

    // 카테고리별 레시피 조회
    public List<RecipeDto> getRecipesByCategory(String category) {
        List<Recipe> recipes = recipeRepository.findAllByCategory(category);
        return recipes.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // 사용자별 레시피 조회
    public List<RecipeDto> getRecipesByUserId(Long userId) {
        List<Recipe> recipes = recipeRepository.findAllByUserId(userId);
        return recipes.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // 제목으로 레시피 검색
    public List<RecipeDto> searchRecipesByTitle(String keyword) {
        List<Recipe> recipes = recipeRepository.findByTitleContaining(keyword);
        return recipes.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // 복합 조건 검색 (제목 + 카테고리)
    public List<RecipeDto> searchRecipes(String keyword, String category) {
        List<Recipe> recipes = recipeRepository.findByTitleAndCategory(keyword, category);
        return recipes.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // 인기 레시피 조회
    public List<RecipeDto> getPopularRecipes(int limit) {
        List<Recipe> recipes = recipeRepository.findPopularRecipes(limit);
        return recipes.stream()
                .map(RecipeDto::new)
                .toList();
    }

    // 페이징 처리된 레시피 목록
    public Page<RecipeDto> getRecipesWithPaging(String keyword, String category,
                                                Visibility visibility, Pageable pageable) {
        Page<Recipe> recipePage = recipeRepository.findRecipesWithPaging(keyword, category, visibility, pageable);
        return recipePage.map(RecipeDto::new);
    }

    // 통계 정보
    public Long getRecipeCountByUserId(Long userId) {
        return recipeRepository.countByUserId(userId);
    }

    public Long getRecipeCountByCategory(String category) {
        return recipeRepository.countByCategory(category);
    }

    // 기존 메서드들 (하위 호환성 유지)
    @Transactional
    public void saveRecipe(RecipeDto recipeDto) {
        createRecipe(recipeDto);
    }
}
