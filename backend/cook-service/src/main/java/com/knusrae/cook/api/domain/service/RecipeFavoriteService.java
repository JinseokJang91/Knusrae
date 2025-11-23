package com.knusrae.cook.api.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeFavorite;
import com.knusrae.cook.api.domain.repository.RecipeFavoriteRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.dto.RecipeFavoriteDto;
import com.knusrae.cook.api.dto.RecipeSimpleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeFavoriteService {
    
    private final RecipeFavoriteRepository recipeFavoriteRepository;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    
    /**
     * 찜 추가
     */
    @Transactional
    public RecipeFavoriteDto addFavorite(Long memberId, Long recipeId) {
        log.info("Adding favorite - memberId: {}, recipeId: {}", memberId, recipeId);
        
        // 이미 찜한 레시피인지 확인
        if (recipeFavoriteRepository.existsByMemberIdAndRecipeId(memberId, recipeId)) {
            throw new IllegalStateException("이미 찜한 레시피입니다.");
        }
        
        // 레시피 존재 여부 확인
        recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));
        
        // 회원 존재 여부 확인
        memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        
        // 찜 추가
        RecipeFavorite favorite = RecipeFavorite.builder()
                .memberId(memberId)
                .recipeId(recipeId)
                .build();
        
        RecipeFavorite savedFavorite = recipeFavoriteRepository.save(favorite);
        log.info("Favorite added successfully - id: {}", savedFavorite.getId());
        
        return RecipeFavoriteDto.from(savedFavorite);
    }
    
    /**
     * 찜 삭제
     */
    @Transactional
    public void removeFavorite(Long memberId, Long recipeId) {
        log.info("Removing favorite - memberId: {}, recipeId: {}", memberId, recipeId);
        
        RecipeFavorite favorite = recipeFavoriteRepository.findByMemberIdAndRecipeId(memberId, recipeId)
                .orElseThrow(() -> new IllegalArgumentException("찜한 레시피가 아닙니다."));
        
        recipeFavoriteRepository.delete(favorite);
        log.info("Favorite removed successfully - id: {}", favorite.getId());
    }
    
    /**
     * 찜 토글 (있으면 삭제, 없으면 추가)
     */
    @Transactional
    public RecipeFavoriteDto toggleFavorite(Long memberId, Long recipeId) {
        log.info("Toggling favorite - memberId: {}, recipeId: {}", memberId, recipeId);
        
        if (recipeFavoriteRepository.existsByMemberIdAndRecipeId(memberId, recipeId)) {
            removeFavorite(memberId, recipeId);
            return null; // 삭제된 경우 null 반환
        } else {
            return addFavorite(memberId, recipeId);
        }
    }
    
    /**
     * 특정 회원의 찜 목록 조회
     */
    public List<RecipeFavoriteDto> getFavoritesByMemberId(Long memberId) {
        log.info("Getting favorites - memberId: {}", memberId);
        
        List<RecipeFavorite> favorites = recipeFavoriteRepository.findByMemberId(memberId);
        
        return favorites.stream()
                .map(favorite -> {
                    Recipe recipe = recipeRepository.findById(favorite.getRecipeId())
                            .orElse(null);
                    
                    if (recipe == null) {
                        return null;
                    }
                    
                    // 회원 정보 조회
                    Member member = memberRepository.findById(recipe.getMemberId())
                            .orElse(null);
                    String memberName = member != null ? member.getName() : "Unknown";
                    
                    // 찜 개수 조회
                    long favoriteCount = recipeFavoriteRepository.countByRecipeId(recipe.getId());
                    
                    RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe, memberName);
                    recipeDto = RecipeSimpleDto.builder()
                            .id(recipeDto.getId())
                            .title(recipeDto.getTitle())
                            .description(recipeDto.getDescription())
                            .thumbnail(recipeDto.getThumbnail())
                            .hits(recipeDto.getHits())
                            .memberId(recipeDto.getMemberId())
                            .memberName(memberName)
                            .visibility(recipeDto.getVisibility())
                            .createdAt(recipeDto.getCreatedAt())
                            .updatedAt(recipeDto.getUpdatedAt())
                            .categories(recipeDto.getCategories())
                            .cookingTips(recipeDto.getCookingTips())
                            .favoriteCount(favoriteCount)
                            .build();
                    
                    return RecipeFavoriteDto.from(favorite, recipeDto);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 회원이 특정 레시피를 찜했는지 확인
     */
    public boolean isFavorite(Long memberId, Long recipeId) {
        return recipeFavoriteRepository.existsByMemberIdAndRecipeId(memberId, recipeId);
    }
    
    /**
     * 특정 레시피의 찜 개수 조회
     */
    public long getFavoriteCount(Long recipeId) {
        return recipeFavoriteRepository.countByRecipeId(recipeId);
    }
}

