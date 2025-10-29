package com.knusrae.cook.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeDetail;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import com.knusrae.cook.api.domain.repository.RecipeStepRepository;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.cook.api.dto.RecipeDetailDto;
import com.knusrae.cook.api.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.dto.RecipeStepDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final ImageStorage imageStorage;
    private final RecipeImageRepository recipeImageRepository;
    private final RecipeStepRepository recipeStepRepository;

    // CREATE - 레시피 생성
    @Transactional
    public RecipeDto createRecipe(RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex) {
        Recipe recipe = recipeDto.toEntity();
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 1) 조리 단계 저장 및 순서 목록 확보
        java.util.List<RecipeDetail> savedDetails = new java.util.ArrayList<>();
        if(!recipeDto.getSteps().isEmpty()) {
            for(RecipeStepDto step : recipeDto.getSteps()) {
                RecipeDetail recipeDetail = step.toEntity(step);
                recipeDetail.setRecipe(savedRecipe);
                RecipeDetail persisted = recipeStepRepository.save(recipeDetail);
                savedDetails.add(persisted);
            }
            // step 값 기준 오름차순 정렬 보장
            savedDetails.sort(java.util.Comparator.comparing(RecipeDetail::getStep));
        }

        // 2) 이미지 저장 및 메타 정보 저장
        if(images != null && !images.isEmpty()) {
            int detailAssignIndex = 0; // 메인 이미지를 제외한 이미지를 단계에 순서대로 매핑하기 위한 인덱스
            for(int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                // 1) 유효성 검사
                validateImage(file);

                // 2) 스토리지 업로드(로컬, TODO S3)
                String relativeDir = "recipes/%d/%s".formatted(savedRecipe.getId(), LocalDate.now());
                ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);

                // 3) 이미지 메타데이터 DB 저장
                RecipeImage img = RecipeImage.builder()
                        .recipe(savedRecipe)
                        .url(uploadResponse.url())        // 예: http://localhost:8080/media/recipes/1/2025-10-10/uuid.jpg
                        .storageKey(uploadResponse.key()) // 예: recipes/1/2025-10-10/uuid.jpg
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .sortOrder(i)
                        .isMainImage(mainImageIndex != null && mainImageIndex == i)
                        .build();
                recipeImageRepository.save(img);

                // 메인 이미지를 제외한 나머지 이미지를 단계 이미지로 매핑
                if (savedDetails.size() > 0) {
                    boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                    if (!isMain && detailAssignIndex < savedDetails.size()) {
                        RecipeDetail targetDetail = savedDetails.get(detailAssignIndex);
                        targetDetail.updateDetail(targetDetail.getContent(), uploadResponse.url());
                        recipeStepRepository.save(targetDetail);
                        detailAssignIndex++;
                    }
                }
            }
        }

        return new RecipeDto(savedRecipe);
    }

    private void validateImage(MultipartFile file) {
        long max = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > max) throw new IllegalArgumentException("파일이 너무 큽니다.");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    // READ - 전체 레시피 목록 조회
    public List<RecipeDto> getRecipeList() {
        List<Recipe> recipeList = recipeRepository.findAllByOrderByCreatedAtDesc();
        List<RecipeDto> recipeDtoList = recipeList.stream()
                .map(RecipeDto::new)
                .toList();
        
        // 각 레시피의 메인 이미지를 조회하여 썸네일로 설정
        for (RecipeDto dto : recipeDtoList) {
            Recipe recipe = recipeRepository.findById(dto.getId())
                    .orElse(null);
            if (recipe != null) {
                List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
                RecipeImage mainImage = images.stream()
                        .filter(RecipeImage::isMainImage)
                        .findFirst()
                        .orElse(images.isEmpty() ? null : images.get(0)); // 메인 이미지가 없으면 첫 번째 이미지
                if (mainImage != null) {
                    dto.setThumbnail(mainImage.getUrl());
                }
            }
        }
        
        return recipeDtoList;
    }

    // READ - 단일 레시피 조회 (조회수 증가)
    @Transactional
    public RecipeDto getRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        recipe.increaseHits(); // 조회수 증가
        RecipeDto dto = new RecipeDto(recipe);
        
        // 메인 이미지를 조회하여 썸네일로 설정
        List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
        RecipeImage mainImage = images.stream()
                .filter(RecipeImage::isMainImage)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0)); // 메인 이미지가 없으면 첫 번째 이미지
        if (mainImage != null) {
            dto.setThumbnail(mainImage.getUrl());
        }
        
        return dto;
    }

    // READ - Recipe 엔티티 조회 (조회수 증가 없음)
    public Recipe getRecipeEntity(Long id) {
        return recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));
    }

    // READ - 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
    @Transactional
    public RecipeDetailDto getRecipeDetail(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        // 조회수 증가
        recipe.increaseHits();
        
        return RecipeDetailDto.fromEntity(recipe);
    }

    // 비활성화: 수정은 현재 미사용

    // DELETE - 레시피 삭제 (물리적 삭제)
    @Transactional
    public void deleteRecipe(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
        for(RecipeImage image : images) {
            imageStorage.deleteByKey(image.getStorageKey());
        }

        recipeRepository.delete(recipe);
    }

    // 비활성화: 논리삭제는 현재 미사용

    // 비활성화: 검색/통계/페이징/레거시 메서드들은 현재 미사용
}
