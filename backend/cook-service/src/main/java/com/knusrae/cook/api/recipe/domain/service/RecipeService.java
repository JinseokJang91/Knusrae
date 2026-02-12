package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.utils.constants.CommonConstants;
import com.knusrae.common.domain.entity.CommonCodeDetail;
import com.knusrae.common.domain.entity.CommonCodeDetailId;
import com.knusrae.common.domain.repository.CommonCodeDetailRepository;
import com.knusrae.cook.api.recipe.domain.entity.RecipeIngredientGroup;
import com.knusrae.cook.api.recipe.domain.entity.RecipeIngredientItem;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeCategory;
import com.knusrae.cook.api.recipe.domain.entity.RecipeDetail;
import com.knusrae.cook.api.recipe.domain.entity.RecipeImage;
import com.knusrae.cook.api.recipe.domain.enums.Status;
import com.knusrae.cook.api.recipe.domain.enums.Visibility;
import com.knusrae.cook.api.recipe.domain.repository.RecipeStepRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeIngredientGroupRepository;
import com.knusrae.cook.api.recipe.dto.*;
import com.knusrae.cook.api.recipe.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeFavoriteRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.domain.repository.FollowRepository;
import com.knusrae.cook.api.recipe.domain.constants.RecipeConstants;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final FollowRepository followRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeIngredientGroupRepository recipeIngredientGroupRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;

    @Transactional
    public RecipeDto createRecipe(RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex) {
        Recipe recipe = recipeDto.toEntity();
        Recipe savedRecipe = recipeRepository.save(recipe);

        saveRecipeCategories(savedRecipe, recipeDto.getCategories());
        saveRecipeCookingTips(savedRecipe, recipeDto.getCookingTips());
        saveIngredientGroups(savedRecipe, recipeDto.getIngredientGroups());

        List<RecipeDetail> savedDetails = new ArrayList<>();
        if(!recipeDto.getSteps().isEmpty()) {
            for(RecipeStepDto step : recipeDto.getSteps()) {
                RecipeDetail recipeDetail = step.toEntity(step);
                recipeDetail.setRecipe(savedRecipe);
                RecipeDetail persisted = recipeStepRepository.save(recipeDetail);
                savedDetails.add(persisted);
            }
            savedDetails.sort(Comparator.comparing(RecipeDetail::getStep));
        }

        String mainImageUrl = null;
        List<String> uploadedImageKeys = new ArrayList<>();
        
        try {
            if(images != null && !images.isEmpty()) {
                int detailAssignIndex = 0;
                String relativeDir = String.format(RecipeConstants.RECIPE_IMAGE_PATH_PATTERN, savedRecipe.getId(), LocalDate.now());
                
                for(int i = 0; i < images.size(); i++) {
                    MultipartFile file = images.get(i);
                    validateImage(file);
                    ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);
                    uploadedImageKeys.add(uploadResponse.key());

                    boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                    RecipeImage img = RecipeImage.builder()
                            .recipe(savedRecipe)
                            .url(uploadResponse.url())
                            .storageKey(uploadResponse.key())
                            .fileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .size(file.getSize())
                            .sortOrder(i)
                            .isMainImage(isMain)
                            .build();
                    recipeImageRepository.save(img);

                    if (isMain) {
                        mainImageUrl = uploadResponse.url();
                    }

                    if (!savedDetails.isEmpty()) {
                        if (!isMain && detailAssignIndex < savedDetails.size()) {
                            RecipeDetail targetDetail = savedDetails.get(detailAssignIndex);
                            targetDetail.updateDetail(targetDetail.getDescription(), uploadResponse.url());
                            recipeStepRepository.save(targetDetail);
                            detailAssignIndex++;
                        }
                    }
                }
                
                if (mainImageUrl != null) {
                    savedRecipe.setThumbnailUrl(mainImageUrl);
                    recipeRepository.save(savedRecipe);
                }
            }
        } catch (Exception e) {
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
            CommonCodeDetail typeDetail = null;
            String customTypeName = null;
            if (groupDto.getDetailCodeId() != null && !groupDto.getDetailCodeId().isEmpty()) {
                CommonCodeDetailId typeId = new CommonCodeDetailId(groupDto.getCodeId(), groupDto.getDetailCodeId());
                typeDetail = commonCodeDetailRepository.findById(typeId)
                        .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 재료 타입입니다. codeId: " + groupDto.getCodeId() + ", detailCodeId: " + groupDto.getDetailCodeId()));
            } else if (groupDto.getCustomTypeName() != null && !groupDto.getCustomTypeName().trim().isEmpty()) {
                customTypeName = groupDto.getCustomTypeName().trim();
            }
            RecipeIngredientGroup group = RecipeIngredientGroup.builder()
                    .typeDetail(typeDetail)
                    .customTypeName(customTypeName)
                    .groupOrder(groupDto.getOrder())
                    .build();
            recipe.addRecipeIngredientGroup(group);
            recipeIngredientGroupRepository.save(group);

            if (groupDto.getItems() != null && !groupDto.getItems().isEmpty()) {
                for (RecipeIngredientItemDto itemDto : groupDto.getItems()) {
                    CommonCodeDetail unitDetail = null;
                    String customUnitName = null;
                    if (itemDto.getDetailCodeId() != null && !itemDto.getDetailCodeId().isEmpty()) {
                        CommonCodeDetailId unitId = new CommonCodeDetailId(itemDto.getCodeId(), itemDto.getDetailCodeId());
                        unitDetail = commonCodeDetailRepository.findById(unitId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                    "존재하지 않는 단위입니다. codeId: " + itemDto.getCodeId() + ", detailCodeId: " + itemDto.getDetailCodeId()));
                    } else if (itemDto.getCustomUnitName() != null && !itemDto.getCustomUnitName().trim().isEmpty()) {
                        customUnitName = itemDto.getCustomUnitName().trim();
                    }
                    String quantity = itemDto.getQuantity() != null && !itemDto.getQuantity().trim().isEmpty() 
                            ? itemDto.getQuantity().trim() 
                            : null;
                    RecipeIngredientItem item = RecipeIngredientItem.builder()
                            .name(itemDto.getName())
                            .quantity(quantity)
                            .unitDetail(unitDetail)
                            .customUnitName(customUnitName)
                            .itemOrder(itemDto.getOrder())
                            .build();
                    group.addItem(item);
                }
            }
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.getSize() > RecipeConstants.MAX_RECIPE_IMAGE_SIZE) {
            throw new IllegalArgumentException("파일이 너무 큽니다. 최대 " + (RecipeConstants.MAX_RECIPE_IMAGE_SIZE / (1024 * 1024)) + "MB까지 가능합니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList(RecipeConstants.ALLOWED_IMAGE_EXTENSIONS);
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용된 형식: " + String.join(", ", allowedExtensions));
            }
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
        if (recipeList.isEmpty()) {
            return new ArrayList<>();
        }
        List<RecipeDto> recipeDtoList = recipeList.stream()
                .map(RecipeDto::new)
                .collect(Collectors.toList());
        List<RecipeImage> allImages = recipeImageRepository.findAllByRecipeIn(recipeList);
        Map<Long, List<RecipeImage>> imagesByRecipeId = allImages.stream()
                .collect(Collectors.groupingBy(img -> img.getRecipe().getId()));
        List<Long> memberIds = recipeList.stream()
                .map(Recipe::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
        List<Object[]> commentCounts = recipeCommentRepository.countByRecipes(recipeList);
        Map<Long, Long> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        Map<Long, Recipe> recipeMap = recipeList.stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        for (RecipeDto dto : recipeDtoList) {
            Long recipeId = dto.getId();
            Recipe recipe = recipeMap.get(recipeId);
            if (recipe == null) {
                continue;
            }
            List<RecipeImage> images = imagesByRecipeId.getOrDefault(recipeId, new ArrayList<>());
            RecipeImage mainImage = images.stream()
                    .filter(RecipeImage::isMainImage)
                    .findFirst()
                    .orElse(images.isEmpty() ? null : images.get(0));
            if (mainImage != null) {
                dto.setThumbnail(mainImage.getUrl());
            }
            Member member = memberMap.get(recipe.getMemberId());
            if (member != null) {
                dto.setMemberName(member.getName());
                dto.setMemberNickname(member.getNickname());
                dto.setMemberProfileImage(member.getProfileImage());
            }
            Long commentCount = commentCountMap.getOrDefault(recipeId, 0L);
            dto.setCommentCount(commentCount);
        }
        return recipeDtoList;
    }

    @Transactional
    public RecipeDetailDto retrieveRecipeDetail(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + id));
        recipe.increaseHits();
        Member member = memberRepository.findById(recipe.getMemberId())
                .orElse(null);
        String memberName = member != null ? member.getName() : "작성자";
        String memberNickname = member != null ? member.getNickname() : null;
        String memberProfileImage = member != null ? member.getProfileImage() : null;
        RecipeDetailDto dto = RecipeDetailDto.fromEntity(recipe, memberName);
        dto.setMemberNickname(memberNickname);
        dto.setMemberProfileImage(memberProfileImage);
        long favoriteCount = recipeFavoriteRepository.countByRecipeId(id);
        RecipeStatsDto stats = dto.getStats();
        RecipeStatsDto updatedStats = RecipeStatsDto.builder()
                .totalComments(stats.getTotalComments())
                .totalReviews(0)
                .averageRating(stats.getAverageRating())
                .totalLikes(stats.getTotalLikes())
                .isLiked(stats.isLiked())
                .favoriteCount(favoriteCount)
                .build();
        dto.setStats(updatedStats);
        return dto;
    }

    @Transactional
    public RecipeDto updateRecipe(Long id, RecipeDto recipeDto, List<MultipartFile> images, Integer mainImageIndex, Long memberId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + id));
        if (!recipe.getMemberId().equals(memberId)) {
            throw new org.springframework.security.access.AccessDeniedException("레시피 작성자만 수정할 수 있습니다.");
        }
        List<RecipeImage> existingImages = recipeImageRepository.findAllByRecipe(recipe);
        recipeStepRepository.deleteAllByRecipe(recipe);
        recipe.clearCategories();
        recipe.clearCookingTips();
        recipe.clearRecipeIngredientGroups();
        entityManager.flush();

        Status statusValue = recipeDto.getStatus() != null ? Status.valueOf(recipeDto.getStatus()) : Status.DRAFT;
        Visibility visibilityValue = recipeDto.getVisibility() != null ? Visibility.valueOf(recipeDto.getVisibility()) : Visibility.PUBLIC;
        recipe.updateRecipe(
                recipeDto.getTitle(),
                recipeDto.getDescription(),
                statusValue,
                visibilityValue
        );
        saveRecipeCategories(recipe, recipeDto.getCategories());
        saveRecipeCookingTips(recipe, recipeDto.getCookingTips());
        saveIngredientGroups(recipe, recipeDto.getIngredientGroups());

        List<RecipeDetail> savedDetails = new ArrayList<>();
        if(!recipeDto.getSteps().isEmpty()) {
            for(RecipeStepDto step : recipeDto.getSteps()) {
                RecipeDetail recipeDetail = step.toEntity(step);
                recipeDetail.setRecipe(recipe);
                RecipeDetail persisted = recipeStepRepository.save(recipeDetail);
                savedDetails.add(persisted);
            }
            savedDetails.sort(Comparator.comparing(RecipeDetail::getStep));
        }

        String mainImageUrl = null;
        List<String> uploadedImageKeys = new ArrayList<>();
        List<String> deletedImageKeys = new ArrayList<>();
        
        try {
            if(images != null && !images.isEmpty()) {
                for(RecipeImage image : existingImages) {
                    deletedImageKeys.add(image.getStorageKey());
                    imageStorage.deleteByKey(image.getStorageKey());
                    recipeImageRepository.delete(image);
                }
                int detailAssignIndex = 0;
                String relativeDir = String.format(RecipeConstants.RECIPE_IMAGE_PATH_PATTERN, recipe.getId(), LocalDate.now());
                for(int i = 0; i < images.size(); i++) {
                    MultipartFile file = images.get(i);
                    validateImage(file);
                    ImageStorage.UploadResponse uploadResponse = imageStorage.upload(file, relativeDir);
                    uploadedImageKeys.add(uploadResponse.key());
                    boolean isMain = (mainImageIndex != null && mainImageIndex == i);
                    RecipeImage img = RecipeImage.builder()
                            .recipe(recipe)
                            .url(uploadResponse.url())
                            .storageKey(uploadResponse.key())
                            .fileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .size(file.getSize())
                            .sortOrder(i)
                            .isMainImage(isMain)
                            .build();
                    recipeImageRepository.save(img);
                    if (isMain) {
                        mainImageUrl = uploadResponse.url();
                    }
                    if (!savedDetails.isEmpty()) {
                        if (!isMain && detailAssignIndex < savedDetails.size()) {
                            RecipeDetail targetDetail = savedDetails.get(detailAssignIndex);
                            targetDetail.updateDetail(targetDetail.getDescription(), uploadResponse.url());
                            recipeStepRepository.save(targetDetail);
                            detailAssignIndex++;
                        }
                    }
                }
                if (mainImageUrl != null) {
                    recipe.setThumbnailUrl(mainImageUrl);
                }
            } else {
                if (!savedDetails.isEmpty() && !existingImages.isEmpty()) {
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
                RecipeImage mainImage = existingImages.stream()
                        .filter(RecipeImage::isMainImage)
                        .findFirst()
                        .orElse(existingImages.isEmpty() ? null : existingImages.get(0));
                if (mainImage != null) {
                    recipe.setThumbnailUrl(mainImage.getUrl());
                }
            }
        } catch (Exception e) {
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

    @Transactional
    public void deleteRecipe(Long id, Long memberId) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + id));
        if (!recipe.getMemberId().equals(memberId)) {
            throw new org.springframework.security.access.AccessDeniedException("레시피 작성자만 삭제할 수 있습니다.");
        }
        recipeFavoriteRepository.deleteByRecipeId(id);
        List<RecipeImage> images = recipeImageRepository.findAllByRecipe(recipe);
        for(RecipeImage image : images) {
            imageStorage.deleteByKey(image.getStorageKey());
        }
        recipeRepository.delete(recipe);
    }
    
    /**
     * 팔로잉 피드 조회 (팔로우한 크리에이터의 레시피)
     * 
     * @param memberId 회원 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 팔로우한 크리에이터의 레시피 목록
     */
    public List<RecipeDto> getFollowingFeed(Long memberId, int page, int size) {
        log.info("팔로잉 피드 조회: memberId={}, page={}, size={}", memberId, page, size);
        
        // 팔로우 중인 크리에이터 ID 목록 조회
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(memberId);
        
        if (followingIds.isEmpty()) {
            log.info("팔로우 중인 크리에이터가 없습니다.");
            return new ArrayList<>();
        }
        
        log.info("팔로우 중인 크리에이터 수: {}", followingIds.size());
        
        // 팔로우한 크리에이터들의 레시피 조회 (최신순)
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Recipe> recipes = recipeRepository.findByMemberIdInAndStatusAndVisibility(
                followingIds, 
                Status.PUBLISHED, 
                Visibility.PUBLIC,
                pageRequest
        );
        
        log.info("팔로잉 피드 레시피 수: {}", recipes.size());
        
        // 기존 메서드를 사용하여 썸네일, 회원 정보, 댓글 수 등을 설정
        return setThumbnailsForRecipeList(recipes);
    }
}
