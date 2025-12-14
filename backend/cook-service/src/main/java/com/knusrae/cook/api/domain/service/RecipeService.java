package com.knusrae.cook.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.utils.constants.CommonConstants;
import com.knusrae.cook.api.domain.entity.CommonCodeDetail;
import com.knusrae.cook.api.domain.entity.CommonCodeDetailId;
import com.knusrae.cook.api.domain.entity.RecipeIngredientGroup;
import com.knusrae.cook.api.domain.entity.RecipeIngredientItem;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeCategory;
import com.knusrae.cook.api.domain.entity.RecipeDetail;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import com.knusrae.cook.api.domain.repository.RecipeStepRepository;
import com.knusrae.cook.api.domain.repository.CommonCodeDetailRepository;
import com.knusrae.cook.api.domain.repository.RecipeIngredientGroupRepository;
import com.knusrae.cook.api.dto.*;
import com.knusrae.cook.api.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.domain.repository.RecipeCommentRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final ImageStorage imageStorage;
    private final RecipeImageRepository recipeImageRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeIngredientGroupRepository recipeIngredientGroupRepository;

    // CREATE - 레시피 생성
    @Transactional
    public RecipeDto createRecipe(RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex) {
        Recipe recipe = recipeDto.toEntity();
        Recipe savedRecipe = recipeRepository.save(recipe);

        // 0) 카테고리 및 요리팁 저장
        saveRecipeCategories(savedRecipe, recipeDto.getCategories());
        saveRecipeCookingTips(savedRecipe, recipeDto.getCookingTips());

        // 0-1) 준비물 저장
        saveIngredientGroups(savedRecipe, recipeDto.getIngredientGroups());

        // 1) 조리 단계 저장 및 순서 목록 확보
        List<RecipeDetail> savedDetails = new ArrayList<>();
        if(!recipeDto.getSteps().isEmpty()) {
            for(RecipeStepDto step : recipeDto.getSteps()) {
                RecipeDetail recipeDetail = step.toEntity(step);
                recipeDetail.setRecipe(savedRecipe);
                RecipeDetail persisted = recipeStepRepository.save(recipeDetail);
                savedDetails.add(persisted);
            }
            // step 값 기준 오름차순 정렬 보장
            savedDetails.sort(Comparator.comparing(RecipeDetail::getStep));
        }

        // 2) 이미지 저장 및 메타 정보 저장
        String mainImageUrl = null; // 메인 이미지 URL 저장용
        if(images != null && !images.isEmpty()) {
            int detailAssignIndex = 0; // 메인 이미지를 제외한 이미지를 단계에 순서대로 매핑하기 위한 인덱스
            for(int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                // 1) 유효성 검사
                validateImage(file);

                // 2) 스토리지 업로드 // 로컬 TODO S3 변경
                String relativeDir = "recipes/%d/%s".formatted(savedRecipe.getId(), LocalDate.now());
                ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);

                // 3) 이미지 메타데이터 DB 저장
                boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                RecipeImage img = RecipeImage.builder()
                        .recipe(savedRecipe)
                        .url(uploadResponse.url())        // 예: http://localhost:8080/media/recipes/1/2025-10-10/uuid.jpg
                        .storageKey(uploadResponse.key()) // 예: recipes/1/2025-10-10/uuid.jpg
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .sortOrder(i)
                        .isMainImage(isMain)
                        .build();
                recipeImageRepository.save(img);

                // 메인 이미지 URL 저장
                if (isMain) {
                    mainImageUrl = uploadResponse.url();
                }

                // 메인 이미지를 제외한 나머지 이미지를 단계 이미지로 매핑
                if (!savedDetails.isEmpty()) {
                    if (!isMain && detailAssignIndex < savedDetails.size()) {
                        RecipeDetail targetDetail = savedDetails.get(detailAssignIndex);
                        targetDetail.updateDetail(targetDetail.getDescription(), uploadResponse.url());
                        recipeStepRepository.save(targetDetail);
                        detailAssignIndex++;
                    }
                }
            }
            
            // 3) 메인 이미지가 지정된 경우 Recipe의 thumbnail 필드 업데이트
            if (mainImageUrl != null) {
                savedRecipe.setThumbnailUrl(mainImageUrl);
                recipeRepository.save(savedRecipe);
            }
        }

        return new RecipeDto(savedRecipe);
    }

    private void saveRecipeCategories(Recipe recipe, List<RecipeCategoryDto> categories) {
        if (categories == null || categories.isEmpty()) {
            return;
        }

        for (RecipeCategoryDto categoryDto : categories) {
            saveRecipeCommonCode(recipe, categoryDto.getCodeId(), categoryDto.getDetailCodeId(), 
                    CommonConstants.COMMON_CODE_GROUP_CATEGORY, "카테고리", 
                    recipe::addRecipeCategory);
        }
    }

    private void saveRecipeCookingTips(Recipe recipe, List<RecipeCookingTipDto> cookingTips) {
        if (cookingTips == null || cookingTips.isEmpty()) {
            return;
        }

        for (RecipeCookingTipDto cookingTipDto : cookingTips) {
            saveRecipeCommonCode(recipe, cookingTipDto.getCodeId(), cookingTipDto.getDetailCodeId(), 
                    CommonConstants.COMMON_CODE_GROUP_COOKINGTIP, "요리팁", 
                    recipe::addRecipeCookingTip);
        }
    }

    private void saveRecipeCommonCode(Recipe recipe, String codeId, String detailCodeId, 
            String expectedCodeGroup, String categoryName, Consumer<RecipeCategory> addFunction) {
        if (codeId == null || detailCodeId == null) {
            return;
        }

        CommonCodeDetailId id = new CommonCodeDetailId(codeId, detailCodeId);

        CommonCodeDetail detail = commonCodeDetailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공통 코드 상세입니다: " + id));

        if (ObjectUtils.isEmpty(detail.getCode()) && !expectedCodeGroup.equals(detail.getCode().getCodeGroup())) {
            throw new IllegalArgumentException(categoryName + "는 codeGroup이 '" + expectedCodeGroup + "'여야 합니다: " + detail.getCode().getCodeGroup());
        }

        RecipeCategory recipeCategory = RecipeCategory.of(recipe, detail);
        addFunction.accept(recipeCategory);
    }

    private void saveIngredientGroups(Recipe recipe, List<RecipeIngredientGroupDto> ingredientGroupDtos) {
        if (ingredientGroupDtos == null || ingredientGroupDtos.isEmpty()) {
            return;
        }

        for (RecipeIngredientGroupDto groupDto : ingredientGroupDtos) {
            // 그룹 타입에 대한 CommonCodeDetail 조회
            CommonCodeDetail typeDetail = null;
            if (groupDto.getDetailCodeId() != null && !groupDto.getDetailCodeId().isEmpty()) {
                // 프론트엔드에서 전달된 detailCodeId를 사용
                CommonCodeDetailId typeId = new CommonCodeDetailId(groupDto.getCodeId(), groupDto.getDetailCodeId());
                
                typeDetail = commonCodeDetailRepository.findById(typeId)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 재료 타입입니다. codeId: " + groupDto.getCodeId() + ", detailCodeId: " + groupDto.getDetailCodeId()));
            }

            // RecipeIngredientGroup 생성
            RecipeIngredientGroup group = RecipeIngredientGroup.builder()
                    .typeDetail(typeDetail)
                    .groupOrder(groupDto.getOrder())
                    .build();
            
            recipe.addRecipeIngredientGroup(group);
            recipeIngredientGroupRepository.save(group);

            // RecipeIngredientItem 저장
            if (groupDto.getItems() != null && !groupDto.getItems().isEmpty()) {
                for (RecipeIngredientItemDto itemDto : groupDto.getItems()) {
                    // 단위에 대한 CommonCodeDetail 조회
                    CommonCodeDetail unitDetail = null;
                    if (itemDto.getDetailCodeId() != null && !itemDto.getDetailCodeId().isEmpty()) {
                        // 프론트엔드에서 전달된 detailCodeId를 사용
                        CommonCodeDetailId unitId = new CommonCodeDetailId(itemDto.getCodeId(), itemDto.getDetailCodeId());
                        unitDetail = commonCodeDetailRepository.findById(unitId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                    "존재하지 않는 단위입니다. codeId: " + itemDto.getCodeId() + ", detailCodeId: " + itemDto.getDetailCodeId()));
                    }

                    // RecipeIngredientItem 생성
                    RecipeIngredientItem item = RecipeIngredientItem.builder()
                            .name(itemDto.getName())
                            .quantity(itemDto.getQuantity())
                            .unitDetail(unitDetail)
                            .itemOrder(itemDto.getOrder())
                            .build();
                    
                    group.addItem(item);
                }
            }
        }
    }

    private void validateImage(MultipartFile file) {
        long max = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > max) throw new IllegalArgumentException("파일이 너무 큽니다.");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
    }

    public List<RecipeDto> listAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findPublishedPublicRecipes();
        return setThumbnailsForRecipeList(recipeList);
    }

    public List<RecipeDto> listMemberRecipes(Long memberId) {
        List<Recipe> recipeList = recipeRepository.findMemberRecipes(memberId);
        return setThumbnailsForRecipeList(recipeList);
    }

    private List<RecipeDto> setThumbnailsForRecipeList(List<Recipe> recipeList) {
        List<RecipeDto> recipeDtoList = recipeList.stream()
                .map(RecipeDto::new)
                .toList();

        for (RecipeDto dto : recipeDtoList) {
            Recipe recipe = recipeRepository.findById(dto.getId()).orElse(null);
            if (recipe != null) {
                List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
                RecipeImage mainImage = images.stream()
                        .filter(RecipeImage::isMainImage)
                        .findFirst()
                        .orElse(images.isEmpty() ? null : images.getFirst());
                if (mainImage != null) {
                    dto.setThumbnail(mainImage.getUrl());
                }

                // Member 정보 조회 및 설정
                Member member = memberRepository.findById(recipe.getMemberId()).orElse(null);
                if (member != null) {
                    dto.setMemberName(member.getName());
                    dto.setMemberNickname(member.getNickname());
                    dto.setMemberProfileImage(member.getProfileImage());
                }
                
                // 댓글 개수 조회 및 설정
                long commentCount = recipeCommentRepository.countByRecipe(recipe);
                dto.setCommentCount(commentCount);
            }
        }

        return recipeDtoList;
    }

    // READ - 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
    @Transactional
    public RecipeDetailDto retrieveRecipeDetail(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        // 조회수 증가
        recipe.increaseHits();
        
        // Member 정보 조회
        Member member = memberRepository.findById(recipe.getMemberId())
                .orElse(null);
        
        String memberName = member != null ? member.getName() : "작성자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;
        
        RecipeDetailDto dto = RecipeDetailDto.fromEntity(recipe, memberName);
        dto.setMemberNickname(memberNickname);
        dto.setMemberProfileImage(memberProfileImage);
        
        return dto;
    }

    // UPDATE - 레시피 수정
    @Transactional
    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex) {
        // 1) 기존 레시피 조회
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));

        // 2) 기존 이미지 백업 (새 이미지가 있을 때만 삭제)
        List<RecipeImage> existingImages = recipeImageRepository.findAllByRecipe(recipe);
        
        // 3) 기존 조리 단계 삭제
        recipeStepRepository.deleteAllByRecipe(recipe);

        // 4) 기존 카테고리, 요리팁, 준비물 삭제
        // orphanRemoval=true가 제대로 작동하도록 명시적으로 clear 후 flush
        recipe.clearCategories();
        recipe.clearCookingTips();
        recipe.clearRecipeIngredientGroups();
        entityManager.flush(); // 영속성 컨텍스트를 DB에 동기화하여 삭제 완료

        // 5) 레시피 기본 정보 업데이트
        Status statusValue = recipeDto.getStatus() != null ? Status.valueOf(recipeDto.getStatus()) : Status.DRAFT;
        Visibility visibilityValue = recipeDto.getVisibility() != null ? Visibility.valueOf(recipeDto.getVisibility()) : Visibility.PUBLIC;
        
        recipe.updateRecipe(
                recipeDto.getTitle(),
                recipeDto.getDescription(),
                statusValue,
                visibilityValue
        );

        // 6) 카테고리, 요리팁, 준비물 저장
        saveRecipeCategories(recipe, recipeDto.getCategories());
        saveRecipeCookingTips(recipe, recipeDto.getCookingTips());
        saveIngredientGroups(recipe, recipeDto.getIngredientGroups());

        // 7) 조리 단계 저장 및 순서 목록 확보
        java.util.List<RecipeDetail> savedDetails = new java.util.ArrayList<>();
        if(!recipeDto.getSteps().isEmpty()) {
            for(RecipeStepDto step : recipeDto.getSteps()) {
                RecipeDetail recipeDetail = step.toEntity(step);
                recipeDetail.setRecipe(recipe);
                RecipeDetail persisted = recipeStepRepository.save(recipeDetail);
                savedDetails.add(persisted);
            }
            // step 값 기준 오름차순 정렬 보장
            savedDetails.sort(java.util.Comparator.comparing(RecipeDetail::getStep));
        }

        // 8) 이미지 처리: 새 이미지가 있으면 기존 이미지 삭제 후 새 이미지 저장, 없으면 기존 이미지 유지
        String mainImageUrl = null; // 메인 이미지 URL 저장용
        if(images != null && !images.isEmpty()) {
            // 새 이미지가 있으면 기존 이미지 삭제
            for(RecipeImage image : existingImages) {
                imageStorage.deleteByKey(image.getStorageKey());
                recipeImageRepository.delete(image);
            }
            
            // 새 이미지 저장
            int detailAssignIndex = 0; // 메인 이미지를 제외한 이미지를 단계에 순서대로 매핑하기 위한 인덱스
            for(int i = 0; i < images.size(); i++) {
                MultipartFile file = images.get(i);

                // 1) 유효성 검사
                validateImage(file);

                // 2) 스토리지 업로드 // 로컬 TODO S3 변경
                String relativeDir = "recipes/%d/%s".formatted(recipe.getId(), LocalDate.now());
                ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);

                // 3) 이미지 메타데이터 DB 저장
                boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                RecipeImage img = RecipeImage.builder()
                        .recipe(recipe)
                        .url(uploadResponse.url())        // 예: http://localhost:8080/media/recipes/1/2025-10-10/uuid.jpg
                        .storageKey(uploadResponse.key()) // 예: recipes/1/2025-10-10/uuid.jpg
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .sortOrder(i)
                        .isMainImage(isMain)
                        .build();
                recipeImageRepository.save(img);

                // 메인 이미지 URL 저장
                if (isMain) {
                    mainImageUrl = uploadResponse.url();
                }

                // 메인 이미지를 제외한 나머지 이미지를 단계 이미지로 매핑
                if (!savedDetails.isEmpty()) {
                    if (!isMain && detailAssignIndex < savedDetails.size()) {
                        RecipeDetail targetDetail = savedDetails.get(detailAssignIndex);
                        targetDetail.updateDetail(targetDetail.getDescription(), uploadResponse.url());
                        recipeStepRepository.save(targetDetail);
                        detailAssignIndex++;
                    }
                }
            }
            
            // 메인 이미지가 지정된 경우 Recipe의 thumbnail 필드 업데이트
            if (mainImageUrl != null) {
                recipe.setThumbnailUrl(mainImageUrl);
            }
        } else {
            // 새 이미지가 없으면 기존 이미지를 단계에 매핑
            if (!savedDetails.isEmpty() && !existingImages.isEmpty()) {
                // 메인 이미지가 아닌 이미지들을 단계에 매핑
                int detailIndex = 0;
                for(RecipeImage image : existingImages) {
                    if (!image.isMainImage() && detailIndex < savedDetails.size()) {
                        RecipeDetail targetDetail = savedDetails.get(detailIndex);
                        targetDetail.updateDetail(targetDetail.getDescription(), image.getUrl());
                        recipeStepRepository.save(targetDetail);
                        detailIndex++;
                    }
                }
            }
            
            // 기존 이미지 유지 시 메인 이미지 확인 및 thumbnail 업데이트
            RecipeImage mainImage = existingImages.stream()
                    .filter(RecipeImage::isMainImage)
                    .findFirst()
                    .orElse(existingImages.isEmpty() ? null : existingImages.get(0));
            if (mainImage != null) {
                recipe.setThumbnailUrl(mainImage.getUrl());
            }
        }

        return new RecipeDto(recipe);
    }

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
}
