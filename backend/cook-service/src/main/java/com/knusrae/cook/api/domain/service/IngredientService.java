package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.Ingredient;
import com.knusrae.cook.api.domain.entity.IngredientGroup;
import com.knusrae.cook.api.domain.entity.IngredientPreparation;
import com.knusrae.cook.api.domain.entity.IngredientStorage;
import com.knusrae.cook.api.domain.repository.*;
import com.knusrae.cook.api.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class IngredientService {
    
    private final IngredientRepository ingredientRepository;
    private final IngredientGroupRepository groupRepository;
    private final IngredientStorageRepository storageRepository;
    private final IngredientPreparationRepository preparationRepository;
    
    /**
     * 모든 재료 그룹 조회
     */
    public List<IngredientGroupDto> getAllGroups() {
        return groupRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(IngredientGroupDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 재료 목록 조회 (그룹, 검색어 필터링 지원)
     */
    public IngredientListResponseDto getIngredients(Long groupId, String searchQuery, int limit, int offset) {
        Specification<Ingredient> spec = Specification.where(null);
        
        if (groupId != null) {
            spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("group").get("id"), groupId)
            );
        }
        
        if (searchQuery != null && !searchQuery.isBlank()) {
            String searchPattern = "%" + searchQuery.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                    cb.like(cb.lower(root.get("name")), searchPattern)
            );
        }
        
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<Ingredient> page = ingredientRepository.findAll(spec, pageable);
        
        // 그룹 목록도 함께 반환
        List<IngredientGroupDto> groups = getAllGroups();
        
        return IngredientListResponseDto.builder()
                .groups(groups)
                .ingredients(page.getContent().stream()
                        .map(IngredientDto::fromEntity)
                        .collect(Collectors.toList()))
                .totalCount(page.getTotalElements())
                .build();
    }
    
    /**
     * 재료 보관법 조회
     */
    public IngredientStorageDto getStorage(Long ingredientId) {
        IngredientStorage storage = storageRepository
                .findByIngredientId(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "재료 보관법을 찾을 수 없습니다: " + ingredientId
                ));
        return IngredientStorageDto.fromEntity(storage);
    }
    
    /**
     * 재료 손질법 조회
     */
    public IngredientPreparationDto getPreparation(Long ingredientId) {
        IngredientPreparation preparation = preparationRepository
                .findByIngredientId(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "재료 손질법을 찾을 수 없습니다: " + ingredientId
                ));
        return IngredientPreparationDto.fromEntity(preparation);
    }
}
