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

import org.springframework.core.io.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import com.knusrae.cook.api.domain.repository.RecipeImageRepository;
import com.knusrae.common.custom.storage.ImageStorage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/recipe")
@RequiredArgsConstructor
@Slf4j
public class RecipeController {

    private final RecipeService recipeService;
    private final ObjectMapper objectMapper;
    private final RecipeImageRepository recipeImageRepository;
    private final ImageStorage imageStorage;

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

    // READ - 전체 레시피 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<RecipeDto>> getRecipeList() {
        List<RecipeDto> recipeList = recipeService.getRecipeList();
        log.info("[LOG][OUTPUT] recipeList: {}", recipeList);

        return ResponseEntity.ok(recipeList);
    }

//    // READ - 단일 레시피 조회 (multipart/form-data로 응답)
//    @GetMapping(value = "/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<MultiValueMap<String, Object>> getRecipe(@PathVariable Long id) {
//        try {
//            // 레시피 정보 조회
//            RecipeDto recipe = recipeService.getRecipe(id);
//
//            // 레시피와 연관된 이미지들 조회
//            List<RecipeImage> recipeImages = recipeImageRepository.findAllByRecipe(
//                    recipeService.getRecipeEntity(id)
//            );
//
//            // MultiValueMap 생성 (multipart 응답용)
//            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
//
//            // 레시피 정보를 JSON으로 추가
//            String recipeJson = objectMapper.writeValueAsString(recipe);
//
//            // JSON 파트에 적절한 헤더 설정
//            HttpHeaders jsonHeaders = new HttpHeaders();
//            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity<String> jsonEntity = new HttpEntity<>(recipeJson, jsonHeaders);
//            parts.add("recipe", jsonEntity);
//
//            // 이미지 파일들을 바이너리로 추가
//            for (int i = 0; i < recipeImages.size(); i++) {
//                RecipeImage recipeImage = recipeImages.get(i);
//                try {
//                    // ImageStorage를 사용하여 이미지 로드
//                    Resource imageResource = imageStorage.loadByKey(recipeImage.getStorageKey());
//
//                    if (imageResource.exists() && imageResource.isReadable()) {
//                        // 이미지 바이트 배열로 읽기
//                        byte[] imageBytes = imageResource.getInputStream().readAllBytes();
//                        ByteArrayResource byteArrayResource = new ByteArrayResource(imageBytes) {
//                            @Override
//                            public String getFilename() {
//                                return recipeImage.getFileName();
//                            }
//                        };
//
//                        // 이미지 파트에 적절한 헤더 설정
//                        HttpHeaders imageHeaders = new HttpHeaders();
//                        imageHeaders.setContentType(MediaType.parseMediaType(recipeImage.getContentType()));
//                        imageHeaders.setContentDispositionFormData("images", recipeImage.getFileName());
//                        HttpEntity<ByteArrayResource> imageEntity = new HttpEntity<>(byteArrayResource, imageHeaders);
//
//                        parts.add("images", imageEntity);
//
//                        // 메인 이미지 인덱스 정보 추가
//                        if (recipeImage.isMainImage()) {
//                            HttpHeaders indexHeaders = new HttpHeaders();
//                            indexHeaders.setContentType(MediaType.TEXT_PLAIN);
//                            HttpEntity<String> indexEntity = new HttpEntity<>(String.valueOf(i), indexHeaders);
//                            parts.add("mainImageIndex", indexEntity);
//                        }
//                    }
//                } catch (Exception e) {
//                    log.warn("Failed to load image: {}", recipeImage.getStorageKey(), e);
//                }
//            }
//
//            return ResponseEntity.ok()
//                    .contentType(MediaType.MULTIPART_FORM_DATA)
//                    .body(parts);
//
//        } catch (Exception e) {
//            log.error("Error retrieving recipe with images: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    // READ - 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecipeDetailDto> getRecipeDetail(@PathVariable Long id) {
        try {
            log.info("Fetching recipe detail for ID: {}", id);
            RecipeDetailDto recipeDetail = recipeService.getRecipeDetail(id);
            log.info("Recipe detail : {}", recipeDetail);
            return ResponseEntity.ok(recipeDetail);
        } catch (Exception e) {
            log.error("Error retrieving recipe detail: {}", e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - 레시피 삭제 (물리적 삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }

    // TODO 비활성화: 수정 API는 현재 미사용
    // TODO 비활성화: 검색/통계/인기/페이징/사용자별 등 확장 API는 현재 미사용
}
