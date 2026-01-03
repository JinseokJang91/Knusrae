package com.knusrae.cook.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.utils.constants.CommonConstants;
import com.knusrae.common.domain.entity.CommonCodeDetail;
import com.knusrae.common.domain.entity.CommonCodeDetailId;
import com.knusrae.common.domain.repository.CommonCodeDetailRepository;
import com.knusrae.cook.api.domain.entity.RecipeIngredientGroup;
import com.knusrae.cook.api.domain.entity.RecipeIngredientItem;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeCategory;
import com.knusrae.cook.api.domain.entity.RecipeDetail;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import com.knusrae.cook.api.domain.repository.RecipeStepRepository;
import com.knusrae.cook.api.domain.repository.RecipeIngredientGroupRepository;
import com.knusrae.cook.api.dto.*;
import com.knusrae.cook.api.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.domain.repository.RecipeFavoriteRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.domain.constants.RecipeConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private final RecipeFavoriteRepository recipeFavoriteRepository;

    /**
     * 레시피 생성
     * 
     * @param recipeDto 레시피 정보 DTO
     * @param images 레시피 이미지 파일 목록
     * @param mainImageIndex 메인 이미지 인덱스 (null인 경우 첫 번째 이미지가 메인)
     * @return 생성된 레시피 DTO
     */
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
        List<String> uploadedImageKeys = new ArrayList<>(); // 업로드된 이미지 키 추적 (롤백용)
        
        try {
            if(images != null && !images.isEmpty()) {
                int detailAssignIndex = 0; // 메인 이미지를 제외한 이미지를 단계에 순서대로 매핑하기 위한 인덱스
                String relativeDir = String.format(RecipeConstants.RECIPE_IMAGE_PATH_PATTERN, savedRecipe.getId(), LocalDate.now());
                
                for(int i = 0; i < images.size(); i++) {
                    MultipartFile file = images.get(i);

                    // 1) 유효성 검사
                    validateImage(file);

                    // 2) 스토리지 업로드 // 로컬 TODO S3 변경
                    ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);
                    uploadedImageKeys.add(uploadResponse.key()); // 업로드된 키 저장

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
        } catch (Exception e) {
            // 이미지 업로드 또는 DB 저장 실패 시 업로드된 이미지 삭제
            log.error("레시피 생성 중 이미지 처리 실패. 업로드된 이미지 삭제 중...", e);
            uploadedImageKeys.forEach(key -> {
                try {
                    imageStorage.deleteByKey(key);
                } catch (Exception deleteException) {
                    log.error("이미지 삭제 실패: key={}", key, deleteException);
                }
            });
            throw e;
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

        if (ObjectUtils.isEmpty(detail.getCode()) || !expectedCodeGroup.equals(detail.getCode().getCodeGroup())) {
            throw new IllegalArgumentException(categoryName + "는 codeGroup이 '" + expectedCodeGroup + "'여야 합니다: " + (detail.getCode() != null ? detail.getCode().getCodeGroup() : "null"));
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
            String customTypeName = null;
            
            if (groupDto.getDetailCodeId() != null && !groupDto.getDetailCodeId().isEmpty()) {
                // 프론트엔드에서 전달된 detailCodeId를 사용 (공통코드 사용)
                CommonCodeDetailId typeId = new CommonCodeDetailId(groupDto.getCodeId(), groupDto.getDetailCodeId());
                
                typeDetail = commonCodeDetailRepository.findById(typeId)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 재료 타입입니다. codeId: " + groupDto.getCodeId() + ", detailCodeId: " + groupDto.getDetailCodeId()));
            } else if (groupDto.getCustomTypeName() != null && !groupDto.getCustomTypeName().trim().isEmpty()) {
                // 직접 입력한 그룹 타입 사용
                customTypeName = groupDto.getCustomTypeName().trim();
            }

            // RecipeIngredientGroup 생성
            RecipeIngredientGroup group = RecipeIngredientGroup.builder()
                    .typeDetail(typeDetail)
                    .customTypeName(customTypeName)
                    .groupOrder(groupDto.getOrder())
                    .build();
            
            recipe.addRecipeIngredientGroup(group);
            recipeIngredientGroupRepository.save(group);

            // RecipeIngredientItem 저장
            if (groupDto.getItems() != null && !groupDto.getItems().isEmpty()) {
                for (RecipeIngredientItemDto itemDto : groupDto.getItems()) {
                    // 단위에 대한 CommonCodeDetail 조회
                    CommonCodeDetail unitDetail = null;
                    String customUnitName = null;
                    
                    if (itemDto.getDetailCodeId() != null && !itemDto.getDetailCodeId().isEmpty()) {
                        // 프론트엔드에서 전달된 detailCodeId를 사용 (공통코드 사용)
                        CommonCodeDetailId unitId = new CommonCodeDetailId(itemDto.getCodeId(), itemDto.getDetailCodeId());
                        unitDetail = commonCodeDetailRepository.findById(unitId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                    "존재하지 않는 단위입니다. codeId: " + itemDto.getCodeId() + ", detailCodeId: " + itemDto.getDetailCodeId()));
                    } else if (itemDto.getCustomUnitName() != null && !itemDto.getCustomUnitName().trim().isEmpty()) {
                        // 직접 입력한 단위 사용
                        customUnitName = itemDto.getCustomUnitName().trim();
                    }

                    // RecipeIngredientItem 생성
                    RecipeIngredientItem item = RecipeIngredientItem.builder()
                            .name(itemDto.getName())
                            .quantity(itemDto.getQuantity())
                            .unitDetail(unitDetail)
                            .customUnitName(customUnitName)
                            .itemOrder(itemDto.getOrder())
                            .build();
                    
                    group.addItem(item);
                }
            }
        }
    }

    /**
     * 이미지 파일 유효성 검증
     * @param file 검증할 이미지 파일
     * @throws IllegalArgumentException 파일 크기, 타입, 확장자가 유효하지 않은 경우
     */
    private void validateImage(MultipartFile file) {
        if (file.getSize() > RecipeConstants.MAX_RECIPE_IMAGE_SIZE) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 " + (RecipeConstants.MAX_RECIPE_IMAGE_SIZE / (1024 * 1024)) + "MB까지 가능합니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 허용된 확장자 검증
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList(RecipeConstants.ALLOWED_IMAGE_EXTENSIONS);
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용된 형식: " + String.join(", ", allowedExtensions));
            }
        }
    }

    /**
     * 전체 레시피 목록 조회 (공개된 레시피만)
     * 
     * @return 공개된 레시피 목록 (썸네일, 작성자 정보, 댓글 개수 포함)
     */
    public List<RecipeDto> listAllRecipes() {
        List<Recipe> recipeList = recipeRepository.findPublishedPublicRecipes();
        return setThumbnailsForRecipeList(recipeList);
    }

    /**
     * 특정 회원의 레시피 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 해당 회원의 레시피 목록 (썸네일, 작성자 정보, 댓글 개수 포함)
     */
    public List<RecipeDto> listMemberRecipes(Long memberId) {
        List<Recipe> recipeList = recipeRepository.findMemberRecipes(memberId);
        return setThumbnailsForRecipeList(recipeList);
    }

    private List<RecipeDto> setThumbnailsForRecipeList(List<Recipe> recipeList) {
        if (recipeList.isEmpty()) {
            return new ArrayList<>();
        }

        List<RecipeDto> recipeDtoList = recipeList.stream()
                .map(RecipeDto::new)
                .collect(Collectors.toList());

        // 1. 모든 이미지를 한 번에 조회 (N+1 문제 해결)
        List<RecipeImage> allImages = recipeImageRepository.findAllByRecipeIn(recipeList);
        Map<Long, List<RecipeImage>> imagesByRecipeId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getRecipe().getId()));

        // 3. 모든 Member ID 추출 및 한 번에 조회 (N+1 문제 해결)
        List<Long> memberIds = recipeList.stream()
                .map(Recipe::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        // 4. 모든 댓글 개수를 한 번에 조회 (N+1 문제 해결)
        List<Object[]> commentCounts = recipeCommentRepository.countByRecipes(recipeList);
        Map<Long, Long> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 5. Recipe를 Map으로 변환 (효율적인 조회를 위해)
        Map<Long, Recipe> recipeMap = recipeList.stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        // 6. DTO에 데이터 설정
        for (RecipeDto dto : recipeDtoList) {
            Long recipeId = dto.getId();
            Recipe recipe = recipeMap.get(recipeId);
            
            if (recipe == null) {
                continue;
            }
            
            // 썸네일 설정
            List<RecipeImage> images = imagesByRecipeId.getOrDefault(recipeId, new ArrayList<>());
            RecipeImage mainImage = images.stream()
                    .filter(RecipeImage::isMainImage)
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0));
            if (mainImage != null) {
                dto.setThumbnail(mainImage.getUrl());
            }

            // Member 정보 설정
            Member member = memberMap.get(recipe.getMemberId());
            if (member != null) {
                dto.setMemberName(member.getName());
                dto.setMemberNickname(member.getNickname());
                dto.setMemberProfileImage(member.getProfileImage());
            }
            
            // 댓글 개수 설정
            Long commentCount = commentCountMap.getOrDefault(recipeId, 0L);
            dto.setCommentCount(commentCount);
        }

        return recipeDtoList;
    }

    /**
     * 레시피 상세 조회 (이미지, 댓글, 리뷰 포함)
     * 조회수 증가를 위해 쓰기 트랜잭션이 필요하므로 @Transactional 유지
     * 
     * @param id 레시피 ID
     * @return 레시피 상세 정보 DTO
     * @throws IllegalArgumentException 레시피를 찾을 수 없는 경우
     */
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
        
        // 찜 개수 조회하여 통계에 포함
        long favoriteCount = recipeFavoriteRepository.countByRecipeId(id);
        RecipeStatsDto stats = dto.getStats();
        RecipeStatsDto updatedStats = RecipeStatsDto.builder()
                .totalComments(stats.getTotalComments())
                .totalReviews(stats.getTotalReviews())
                .averageRating(stats.getAverageRating())
                .totalLikes(stats.getTotalLikes())
                .isLiked(stats.isLiked())
                .favoriteCount(favoriteCount)
                .build();
        dto.setStats(updatedStats);
        
        return dto;
    }

    /**
     * 레시피 수정
     * 작성자만 수정 가능하며, 이미지, 카테고리, 요리팁, 준비물, 조리 단계를 모두 업데이트
     * 
     * @param id 레시피 ID
     * @param recipeDto 수정할 레시피 정보 DTO
     * @param images 새 이미지 파일 목록 (null인 경우 기존 이미지 유지)
     * @param mainImageIndex 메인 이미지 인덱스
     * @param memberId 수정 요청한 회원 ID (작성자 확인용)
     * @return 수정된 레시피 DTO
     * @throws IllegalArgumentException 레시피를 찾을 수 없는 경우
     * @throws org.springframework.security.access.AccessDeniedException 작성자가 아닌 경우
     */
    @Transactional
    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex, Long memberId) {
        // 1) 기존 레시피 조회
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + id));
        
        // 2) 작성자 확인
        if (!recipe.getMemberId().equals(memberId)) {
            throw new org.springframework.security.access.AccessDeniedException("레시피 작성자만 수정할 수 있습니다.");
        }

        // 3) 기존 이미지 백업 (새 이미지가 있을 때만 삭제)
        List<RecipeImage> existingImages = recipeImageRepository.findAllByRecipe(recipe);
        
        // 4) 기존 조리 단계 삭제
        recipeStepRepository.deleteAllByRecipe(recipe);

        // 5) 기존 카테고리, 요리팁, 준비물 삭제
        // orphanRemoval=true가 제대로 작동하도록 명시적으로 clear 후 flush
        recipe.clearCategories();
        recipe.clearCookingTips();
        recipe.clearRecipeIngredientGroups();
        entityManager.flush(); // 영속성 컨텍스트를 DB에 동기화하여 삭제 완료

        // 6) 레시피 기본 정보 업데이트
        Status statusValue = recipeDto.getStatus() != null ? Status.valueOf(recipeDto.getStatus()) : Status.DRAFT;
        Visibility visibilityValue = recipeDto.getVisibility() != null ? Visibility.valueOf(recipeDto.getVisibility()) : Visibility.PUBLIC;
        
        recipe.updateRecipe(
                recipeDto.getTitle(),
                recipeDto.getDescription(),
                statusValue,
                visibilityValue
        );

        // 7) 카테고리, 요리팁, 준비물 저장
        saveRecipeCategories(recipe, recipeDto.getCategories());
        saveRecipeCookingTips(recipe, recipeDto.getCookingTips());
        saveIngredientGroups(recipe, recipeDto.getIngredientGroups());

        // 8) 조리 단계 저장 및 순서 목록 확보
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

        // 9) 이미지 처리: 새 이미지가 있으면 기존 이미지 삭제 후 새 이미지 저장, 없으면 기존 이미지 유지
        String mainImageUrl = null; // 메인 이미지 URL 저장용
        List<String> uploadedImageKeys = new ArrayList<>(); // 업로드된 이미지 키 추적 (롤백용)
        List<String> deletedImageKeys = new ArrayList<>(); // 삭제된 기존 이미지 키 추적 (롤백용 - 복구 불가능하므로 로깅만)
        
        try {
            if(images != null && !images.isEmpty()) {
                // 새 이미지가 있으면 기존 이미지 삭제
                for(RecipeImage image : existingImages) {
                    deletedImageKeys.add(image.getStorageKey());
                    imageStorage.deleteByKey(image.getStorageKey());
                    recipeImageRepository.delete(image);
                }
                
                // 새 이미지 저장
                int detailAssignIndex = 0; // 메인 이미지를 제외한 이미지를 단계에 순서대로 매핑하기 위한 인덱스
                String relativeDir = String.format(RecipeConstants.RECIPE_IMAGE_PATH_PATTERN, recipe.getId(), LocalDate.now());
                
                for(int i = 0; i < images.size(); i++) {
                    MultipartFile file = images.get(i);

                    // 1) 유효성 검사
                    validateImage(file);

                    // 2) 스토리지 업로드 // 로컬 TODO S3 변경
                    ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);
                    uploadedImageKeys.add(uploadResponse.key()); // 업로드된 키 저장

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
        } catch (Exception e) {
            // 이미지 업로드 또는 DB 저장 실패 시 업로드된 새 이미지 삭제
            log.error("레시피 수정 중 이미지 처리 실패. 업로드된 새 이미지 삭제 중... (기존 이미지는 이미 삭제됨: {})", deletedImageKeys, e);
            uploadedImageKeys.forEach(key -> {
                try {
                    imageStorage.deleteByKey(key);
                } catch (Exception deleteException) {
                    log.error("이미지 삭제 실패: key={}", key, deleteException);
                }
            });
            throw e;
        }

        return new RecipeDto(recipe);
    }

    /**
     * 레시피 삭제 (물리적 삭제)
     * 작성자만 삭제 가능
     * 
     * @param id 레시피 ID
     * @param memberId 삭제 요청한 회원 ID (작성자 확인용)
     * @throws IllegalArgumentException 레시피를 찾을 수 없는 경우
     * @throws org.springframework.security.access.AccessDeniedException 작성자가 아닌 경우
     */
    // DELETE - 레시피 삭제 (물리적 삭제)
    @Transactional
    public void deleteRecipe(Long id, Long memberId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + id));
        
        // 작성자 확인
        if (!recipe.getMemberId().equals(memberId)) {
            throw new org.springframework.security.access.AccessDeniedException("레시피 작성자만 삭제할 수 있습니다.");
        }

        // 1) 관련된 찜 목록 삭제 (외래키 제약조건 해결)
        recipeFavoriteRepository.deleteByRecipeId(id);

        // 2) 이미지 삭제
        List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
        for(RecipeImage image : images) {
            imageStorage.deleteByKey(image.getStorageKey());
        }

        // 3) 레시피 삭제
        recipeRepository.delete(recipe);
    }
}
