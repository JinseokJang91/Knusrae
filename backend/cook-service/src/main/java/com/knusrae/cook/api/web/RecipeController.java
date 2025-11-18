package com.knusrae.cook.api.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.RecipeDetailDto;
import com.knusrae.cook.api.domain.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;

    // CREATE - 레시피 생성 (이미지 포함)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> createRecipe(
            @RequestPart(value = "recipe") String recipeJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex
    ) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        log.info("[LOG][INPUT] Creating recipe: {}, images: {}, mainImageIndex: {}", recipeJson, images, mainImageIndex);
        RecipeDto dto = mapper.readValue(recipeJson, RecipeDto.class);
        log.info("[LOG][INPUT] Parsed recipeDto: {}", dto.toString());
        RecipeDto created = recipeService.createRecipe(dto, images, mainImageIndex);
        log.info("[LOG][OUTPUT] Created recipe: {}", created.toString());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // READ - 전체 레시피 목록 조회(로그인 유저용)
    @GetMapping("/list/member/{memberId}")
    public ResponseEntity<List<RecipeDto>> listMemberRecipes(@PathVariable Long memberId) {
        List<RecipeDto> recipeList = recipeService.listMemberRecipes(memberId);
        log.info("[LOG][OUTPUT] member recipeList: {}", recipeList);

        return ResponseEntity.ok(recipeList);
    }

    // READ - 전체 레시피 목록 조회(애플리케이션 사용자용)
    @GetMapping("/list/all")
    public ResponseEntity<List<RecipeDto>> listAllRecipes() {
        List<RecipeDto> recipeList = recipeService.listAllRecipes();
        log.info("[LOG][OUTPUT] all recipeList: {}", recipeList);

        return ResponseEntity.ok(recipeList);
    }

    // READ - 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecipeDetailDto> retrieveRecipeDetail(@PathVariable Long id) {
        try {
            log.info("Fetching recipe detail for ID: {}", id);
            RecipeDetailDto recipeDetail = recipeService.retrieveRecipeDetail(id);
            log.info("Recipe detail : {}", recipeDetail);
            return ResponseEntity.ok(recipeDetail);
        } catch (Exception e) {
            log.error("Error retrieving recipe detail: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // UPDATE - 레시피 수정 (이미지 포함)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeDto> updateRecipe(
            @PathVariable Long id,
            @RequestPart(value = "recipe") String recipeJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "mainImageIndex", required = false) Integer mainImageIndex
    ) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        log.info("[LOG][INPUT] Updating recipe ID: {}, recipe: {}, images: {}, mainImageIndex: {}", id, recipeJson, images, mainImageIndex);
        RecipeDto dto = mapper.readValue(recipeJson, RecipeDto.class);
        log.info("[LOG][INPUT] Parsed recipeDto: {}", dto.toString());
        RecipeDto updated = recipeService.updateRecipe(id, dto, images, mainImageIndex);
        log.info("[LOG][OUTPUT] Updated recipe: {}", updated.toString());

        return ResponseEntity.ok(updated);
    }

    // DELETE - 레시피 삭제 (물리적 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // TODO 비활성화: 검색/통계/인기/페이징/사용자별 등 확장 API는 현재 미사용
}
