package com.knusrae.cook.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.cook.api.domain.constants.RecipeConstants;
import com.knusrae.cook.api.domain.entity.Ingredient;
import com.knusrae.cook.api.domain.entity.IngredientGroup;
import com.knusrae.cook.api.domain.entity.IngredientPreparation;
import com.knusrae.cook.api.domain.entity.IngredientStorage;
import com.knusrae.cook.api.domain.repository.*;
import com.knusrae.cook.api.dto.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminIngredientService {

    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository groupRepository;
    private final IngredientStorageRepository storageRepository;
    private final IngredientPreparationRepository preparationRepository;
    private final ImageStorage imageStorage;

    /**
     * 재료 그룹 등록
     */
    public IngredientGroupDto createGroup(String name, String imageUrl, Integer sortOrder) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("재료 그룹명은 필수입니다.");
        }
        int order = Optional.ofNullable(sortOrder).orElse(0);
        IngredientGroup group = IngredientGroup.builder()
                .name(name.trim())
                .imageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl.trim() : null)
                .sortOrder(order)
                .build();
        IngredientGroup saved = groupRepository.save(group);
        return IngredientGroupDto.fromEntity(saved);
    }

    /**
     * 재료 등록
     */
    public IngredientDto createIngredient(Long groupId, String name, String imageUrl, Integer sortOrder) {
        if (groupId == null) {
            throw new IllegalArgumentException("재료 그룹은 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("재료명은 필수입니다.");
        }
        IngredientGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("재료 그룹을 찾을 수 없습니다: " + groupId));
        int order = Optional.ofNullable(sortOrder).orElse(0);
        Ingredient ingredient = Ingredient.builder()
                .name(name.trim())
                .group(group)
                .imageUrl(imageUrl != null && !imageUrl.isBlank() ? imageUrl.trim() : null)
                .sortOrder(order)
                .build();
        Ingredient saved = ingredientRepository.save(ingredient);
        return IngredientDto.fromEntity(saved);
    }
    
    /**
     * 재료 보관법 등록
     */
    public IngredientStorageDto createStorage(Long ingredientId, String content, String summary, Long adminId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException("재료를 찾을 수 없습니다: " + ingredientId));
        
        if (storageRepository.existsByIngredientId(ingredientId)) {
            throw new IllegalStateException("이미 보관법이 등록된 재료입니다: " + ingredientId);
        }
        
        IngredientStorage storage = IngredientStorage.builder()
                .ingredient(ingredient)
                .content(content)
                .summary(summary)
                .createdBy(adminId)
                .build();
        
        IngredientStorage saved = storageRepository.save(storage);
        return IngredientStorageDto.fromEntity(saved);
    }
    
    /**
     * 재료 손질법 등록
     */
    public IngredientPreparationDto createPreparation(Long ingredientId, String content, String summary, Long adminId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException("재료를 찾을 수 없습니다: " + ingredientId));
        
        if (preparationRepository.existsByIngredientId(ingredientId)) {
            throw new IllegalStateException("이미 손질법이 등록된 재료입니다: " + ingredientId);
        }
        
        IngredientPreparation preparation = IngredientPreparation.builder()
                .ingredient(ingredient)
                .content(content)
                .summary(summary)
                .createdBy(adminId)
                .build();
        
        IngredientPreparation saved = preparationRepository.save(preparation);
        return IngredientPreparationDto.fromEntity(saved);
    }
    
    /**
     * 재료 보관법 수정
     */
    public IngredientStorageDto updateStorage(Long storageId, String content, String summary) {
        IngredientStorage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new EntityNotFoundException("보관법을 찾을 수 없습니다: " + storageId));
        
        storage.updateStorage(content, summary);
        IngredientStorage updated = storageRepository.save(storage);
        return IngredientStorageDto.fromEntity(updated);
    }
    
    /**
     * 재료 손질법 수정
     */
    public IngredientPreparationDto updatePreparation(Long preparationId, String content, String summary) {
        IngredientPreparation preparation = preparationRepository.findById(preparationId)
                .orElseThrow(() -> new EntityNotFoundException("손질법을 찾을 수 없습니다: " + preparationId));
        
        preparation.updatePreparation(content, summary);
        IngredientPreparation updated = preparationRepository.save(preparation);
        return IngredientPreparationDto.fromEntity(updated);
    }
    
    /**
     * 재료 보관법 삭제
     */
    public void deleteStorage(Long storageId) {
        if (!storageRepository.existsById(storageId)) {
            throw new EntityNotFoundException("보관법을 찾을 수 없습니다: " + storageId);
        }
        storageRepository.deleteById(storageId);
    }
    
    /**
     * 재료 손질법 삭제
     */
    public void deletePreparation(Long preparationId) {
        if (!preparationRepository.existsById(preparationId)) {
            throw new EntityNotFoundException("손질법을 찾을 수 없습니다: " + preparationId);
        }
        preparationRepository.deleteById(preparationId);
    }

    /**
     * 본문용 이미지 업로드 (에디터 addImageBlobHook).
     * 저장 경로: content/YYYY-MM-DD
     *
     * @return 업로드된 이미지 공개 URL
     */
    public String uploadContentImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 없습니다.");
        }
        validateContentImage(file);
        String relativeDir = "content/" + LocalDate.now();
        ImageStorage.UploadResponse res = imageStorage.upload(file, relativeDir);
        log.debug("Uploaded content image: {}", res.key());
        return res.url();
    }

    private void validateContentImage(MultipartFile file) {
        if (file.getSize() > RecipeConstants.MAX_COMMENT_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "파일이 너무 큽니다. 최대 " + (RecipeConstants.MAX_COMMENT_IMAGE_SIZE / (1024 * 1024)) + "MB까지 가능합니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        String filename = file.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowed = Arrays.asList(RecipeConstants.ALLOWED_IMAGE_EXTENSIONS);
            if (!allowed.contains(ext)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 허용: " + String.join(", ", allowed));
            }
        }
    }
}
