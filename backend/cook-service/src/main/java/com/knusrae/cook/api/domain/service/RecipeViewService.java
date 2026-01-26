package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeView;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.domain.repository.RecipeViewRepository;
import com.knusrae.cook.api.dto.RecipeSimpleDto;
import com.knusrae.cook.api.dto.RecipeViewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeViewService {
    
    private final RecipeViewRepository recipeViewRepository;
    private final RecipeRepository recipeRepository;
    
    /**
     * 레시피 조회 기록 생성 또는 갱신
     * 
     * @param memberId 회원 ID
     * @param recipeId 레시피 ID
     * @return 조회 기록 DTO와 신규 생성 여부
     */
    @Transactional
    public Map<String, Object> createOrUpdateRecipeView(Long memberId, Long recipeId) {
        // 레시피 존재 여부 확인
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. ID: " + recipeId));
        
        // 기존 조회 기록이 있는지 확인
        RecipeView recipeView = recipeViewRepository.findByMemberIdAndRecipeId(memberId, recipeId)
                .orElse(null);
        
        boolean isNew = false;
        
        if (recipeView == null) {
            // 새로운 조회 기록 생성
            recipeView = RecipeView.builder()
                    .memberId(memberId)
                    .recipeId(recipeId)
                    .viewedAt(LocalDateTime.now())
                    .build();
            isNew = true;
        } else {
            // 기존 조회 기록 갱신
            recipeView.updateViewedAt();
        }
        
        RecipeView savedView = recipeViewRepository.save(recipeView);
        
        // 레시피 조회수 증가 (Recipe 엔티티의 hits 필드)
        if (isNew) {
            recipe.increaseHits();
            recipeRepository.save(recipe);
        }
        
        log.info("Recipe view {} for member: {}, recipe: {}", isNew ? "created" : "updated", memberId, recipeId);
        
        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("view", RecipeViewDto.from(savedView));
        result.put("isNew", isNew);
        
        return result;
    }
    
    /**
     * 특정 회원의 최근 본 레시피 목록 조회
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수
     * @param offset 건너뛸 개수
     * @return 최근 본 레시피 목록
     */
    public Map<String, Object> getRecentViews(Long memberId, int limit, int offset) {
        // 페이징 설정
        Pageable pageable = PageRequest.of(offset / limit, limit);
        
        // 조회 기록 가져오기
        List<RecipeView> views = recipeViewRepository.findByMemberIdOrderByViewedAtDesc(memberId, pageable);
        
        // 레시피 ID 목록 추출
        List<Long> recipeIds = views.stream()
                .map(RecipeView::getRecipeId)
                .collect(Collectors.toList());
        
        // 레시피 정보 조회
        List<Recipe> recipes = recipeRepository.findAllById(recipeIds);
        Map<Long, Recipe> recipeMap = recipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));
        
        // DTO 변환 (레시피 정보 포함)
        List<RecipeViewDto> viewDtos = views.stream()
                .map(view -> {
                    Recipe recipe = recipeMap.get(view.getRecipeId());
                    if (recipe != null) {
                        RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe);
                        return RecipeViewDto.from(view, recipeDto);
                    } else {
                        return RecipeViewDto.from(view);
                    }
                })
                .collect(Collectors.toList());
        
        // 전체 개수
        long totalCount = recipeViewRepository.countByMemberId(memberId);
        
        // 결과 반환
        Map<String, Object> result = new HashMap<>();
        result.put("views", viewDtos);
        result.put("totalCount", totalCount);
        
        log.info("Retrieved {} recent views for member: {} (total: {})", viewDtos.size(), memberId, totalCount);
        
        return result;
    }
    
    /**
     * 특정 회원의 모든 조회 기록 삭제
     * 
     * @param memberId 회원 ID
     */
    @Transactional
    public void deleteAllViewsByMember(Long memberId) {
        recipeViewRepository.deleteByMemberId(memberId);
        log.info("Deleted all views for member: {}", memberId);
    }
    
    /**
     * 특정 회원의 최근 본 카테고리 조회 (추천 알고리즘용)
     * 
     * @param memberId 회원 ID
     * @param daysAgo 몇 일 전까지의 데이터를 조회할지
     * @param limit 조회할 카테고리 개수
     * @return 카테고리 코드와 횟수 맵
     */
    public List<Object[]> getRecentCategories(Long memberId, int daysAgo, int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysAgo);
        Pageable pageable = PageRequest.of(0, limit);
        
        return recipeViewRepository.findRecentCategoriesByMember(memberId, since, pageable);
    }
}
