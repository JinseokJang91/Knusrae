package com.knusrae.cook.api.ingredient.domain.service;

import com.knusrae.cook.api.ingredient.domain.entity.Ingredient;
import com.knusrae.cook.api.ingredient.domain.entity.IngredientPreparation;
import com.knusrae.cook.api.ingredient.domain.entity.IngredientStorage;
import com.knusrae.cook.api.ingredient.domain.repository.IngredientGroupRepository;
import com.knusrae.cook.api.ingredient.domain.repository.IngredientPreparationRepository;
import com.knusrae.cook.api.ingredient.domain.repository.IngredientRepository;
import com.knusrae.cook.api.ingredient.domain.repository.IngredientStorageRepository;
import com.knusrae.cook.api.ingredient.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    public List<IngredientGroupDto> getAllGroups() {
        return groupRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(IngredientGroupDto::fromEntity)
                .collect(Collectors.toList());
    }

    private List<IngredientGroupDto> getGroupsForType(String type) {
        if ("storage".equalsIgnoreCase(type)) {
            List<Long> groupIds = storageRepository.findDistinctGroupIds();
            if (groupIds.isEmpty()) {
                return Collections.emptyList();
            }
            return groupRepository.findAllByIdInOrderBySortOrderAsc(groupIds)
                    .stream()
                    .map(IngredientGroupDto::fromEntity)
                    .collect(Collectors.toList());
        }
        if ("preparation".equalsIgnoreCase(type)) {
            List<Long> groupIds = preparationRepository.findDistinctGroupIds();
            if (groupIds.isEmpty()) {
                return Collections.emptyList();
            }
            return groupRepository.findAllByIdInOrderBySortOrderAsc(groupIds)
                    .stream()
                    .map(IngredientGroupDto::fromEntity)
                    .collect(Collectors.toList());
        }
        return getAllGroups();
    }

    public IngredientListResponseDto getIngredients(Long groupId, String searchQuery, String type, int limit, int offset) {
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
        List<IngredientGroupDto> groups = getAllGroups();
        return IngredientListResponseDto.builder()
                .groups(groups)
                .ingredients(page.getContent().stream()
                        .map(IngredientDto::fromEntity)
                        .collect(Collectors.toList()))
                .totalCount(page.getTotalElements())
                .build();
    }

    public IngredientStorageDto getStorage(Long ingredientId) {
        IngredientStorage storage = storageRepository
                .findByIngredientId(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "재료 보관법을 찾을 수 없습니다: " + ingredientId
                ));
        return IngredientStorageDto.fromEntity(storage);
    }

    public IngredientPreparationDto getPreparation(Long ingredientId) {
        IngredientPreparation preparation = preparationRepository
                .findByIngredientId(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "재료 손질법을 찾을 수 없습니다: " + ingredientId
                ));
        return IngredientPreparationDto.fromEntity(preparation);
    }
}
