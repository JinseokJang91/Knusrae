package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipePopularity;
import com.knusrae.cook.api.domain.repository.*;
import com.knusrae.cook.api.dto.PopularRecipeDto;
import com.knusrae.cook.api.dto.PopularityStatsDto;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.common.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PopularRecipeService {
    
    private final RecipePopularityRepository recipePopularityRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final MemberRepository memberRepository;
    private final RecipeCommentRepository recipeCommentRepository;
    
    /**
     * 인기 레시피 목록 조회
     * 
     * @param limit 조회할 개수
     * @param period 기간 (24h, 7d, 30d) - 현재는 popularity_score 기준
     * @return 인기 레시피 목록
     */
    public List<PopularRecipeDto> getPopularRecipes(int limit, String period) {
        log.debug("Fetching popular recipes: limit={}, period={}", limit, period);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<RecipePopularity> popularityList = switch (period) {
            case "24h" -> recipePopularityRepository.findTopByHits24h(pageable);
            case "7d" -> recipePopularityRepository.findTopByHits7d(pageable);
            default -> recipePopularityRepository.findTopByPopularityScore(pageable);
        };
        
        // 기간에 따른 정렬 기준 선택

        // DTO 변환
        List<PopularRecipeDto> result = new ArrayList<>();
        int rank = 1;
        
        for (RecipePopularity popularity : popularityList) {
            Recipe recipe = popularity.getRecipe();
            
            // RecipeDto 생성
            RecipeDto recipeDto = convertToRecipeDto(recipe);
            
            // PopularityStatsDto 생성
            PopularityStatsDto statsDto = PopularityStatsDto.from(popularity);
            
            // PopularRecipeDto 생성
            PopularRecipeDto popularRecipeDto = PopularRecipeDto.builder()
                    .rank(rank)
                    .previousRank(null) // TODO: 순위 추적 기능 추가 시 구현
                    .trendStatus("SAME") // TODO: 순위 추적 기능 추가 시 구현
                    .recipe(recipeDto)
                    .popularityStats(statsDto)
                    .calculatedAt(popularity.getCalculatedAt())
                    .build();
            
            result.add(popularRecipeDto);
            rank++;
        }
        
        log.info("Retrieved {} popular recipes", result.size());
        return result;
    }
    
    /**
     * Recipe Entity를 RecipeDto로 변환
     */
    private RecipeDto convertToRecipeDto(Recipe recipe) {
        // Recipe 생성자를 사용하여 기본 필드 매핑
        RecipeDto dto = new RecipeDto(recipe);
        
        // 썸네일 이미지 설정
        recipeImageRepository.findMainImageByRecipeId(recipe.getId())
                .ifPresent(image -> dto.setThumbnail(image.getUrl()));
        
        // 작성자 정보 설정
        memberRepository.findById(recipe.getMemberId())
                .ifPresent(member -> {
                    dto.setMemberName(member.getName());
                    dto.setMemberNickname(member.getNickname());
                    dto.setMemberProfileImage(member.getProfileImage());
                });
        
        // 댓글 개수 설정
        long commentCount = recipeCommentRepository.countByRecipeId(recipe.getId());
        dto.setCommentCount(commentCount);
        
        return dto;
    }
}

