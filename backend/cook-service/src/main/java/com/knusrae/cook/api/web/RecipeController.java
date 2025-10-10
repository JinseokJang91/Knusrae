package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.Visibility;
import com.knusrae.cook.api.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    // CREATE - 레시피 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> createRecipe(
            @Valid @RequestPart(value = "steps") RecipeDto recipeDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex
    ) {
        log.info("recipeDto: {}, images: {}, mainImageIndex: {}", recipeDto, images, mainImageIndex);
        RecipeDto createdRecipe = recipeService.createRecipe(recipeDto, images, mainImageIndex);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipe);
    }

    // READ - 전체 레시피 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<RecipeDto>> getRecipeList() {
        List<RecipeDto> recipeList = recipeService.getRecipeList();
        return ResponseEntity.ok(recipeList);
    }

    // READ - 단일 레시피 조회
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipe(@PathVariable Long id) {
        RecipeDto recipe = recipeService.getRecipe(id);
        return ResponseEntity.ok(recipe);
    }

    // UPDATE - 레시피 수정
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable Long id,
                                                  @Valid @RequestBody RecipeDto recipeDto) {
        RecipeDto updatedRecipe = recipeService.updateRecipe(id, recipeDto);
        return ResponseEntity.ok(updatedRecipe);
    }

    // DELETE - 레시피 삭제 (물리적 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE - 레시피 비활성화 (논리적 삭제)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateRecipe(@PathVariable Long id) {
        recipeService.deactivateRecipe(id);
        return ResponseEntity.ok().build();
    }

    // QueryDSL 기반 복합 조회 API들

    // 카테고리별 레시피 조회
    @GetMapping("/category/{category}")
    public ResponseEntity<List<RecipeDto>> getRecipesByCategory(@PathVariable String category) {
        List<RecipeDto> recipes = recipeService.getRecipesByCategory(category);
        return ResponseEntity.ok(recipes);
    }

    // 사용자별 레시피 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecipeDto>> getRecipesByUserId(@PathVariable Long userId) {
        List<RecipeDto> recipes = recipeService.getRecipesByUserId(userId);
        return ResponseEntity.ok(recipes);
    }

    // 제목으로 레시피 검색
    @GetMapping("/search")
    public ResponseEntity<List<RecipeDto>> searchRecipesByTitle(@RequestParam String keyword) {
        List<RecipeDto> recipes = recipeService.searchRecipesByTitle(keyword);
        return ResponseEntity.ok(recipes);
    }

    // 복합 조건 검색 (제목 + 카테고리)
    @GetMapping("/search/advanced")
    public ResponseEntity<List<RecipeDto>> searchRecipes(@RequestParam(required = false) String keyword,
                                                         @RequestParam(required = false) String category) {
        List<RecipeDto> recipes = recipeService.searchRecipes(keyword, category);
        return ResponseEntity.ok(recipes);
    }

    // 인기 레시피 조회
    @GetMapping("/popular")
    public ResponseEntity<List<RecipeDto>> getPopularRecipes(@RequestParam(defaultValue = "10") int limit) {
        List<RecipeDto> recipes = recipeService.getPopularRecipes(limit);
        return ResponseEntity.ok(recipes);
    }

    // 페이징 처리된 레시피 목록
    @GetMapping("/page")
    public ResponseEntity<Page<RecipeDto>> getRecipesWithPaging(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Visibility visibility,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RecipeDto> recipePage = recipeService.getRecipesWithPaging(keyword, category, visibility, pageable);
        return ResponseEntity.ok(recipePage);
    }

    // 통계 API들

    // 사용자별 레시피 개수
    @GetMapping("/count/user/{userId}")
    public ResponseEntity<Long> getRecipeCountByUserId(@PathVariable Long userId) {
        Long count = recipeService.getRecipeCountByUserId(userId);
        return ResponseEntity.ok(count);
    }

    // 카테고리별 레시피 개수
    @GetMapping("/count/category/{category}")
    public ResponseEntity<Long> getRecipeCountByCategory(@PathVariable String category) {
        Long count = recipeService.getRecipeCountByCategory(category);
        return ResponseEntity.ok(count);
    }

    // 기존 API들 (하위 호환성 유지)
    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveRecipe(
            @Valid @RequestPart RecipeDto recipeDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex
    ) {
        recipeService.saveRecipe(recipeDto, images, mainImageIndex);
        return ResponseEntity.ok().build();
    }
}
