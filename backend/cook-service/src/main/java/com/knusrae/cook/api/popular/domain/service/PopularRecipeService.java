package com.knusrae.cook.api.popular.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipePopularity;
import com.knusrae.cook.api.recipe.domain.entity.RecipePopularityHistory;
import com.knusrae.cook.api.recipe.domain.repository.*;
import com.knusrae.cook.api.popular.dto.PopularRecipeDto;
import com.knusrae.cook.api.popular.dto.PopularityStatsDto;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.recipe.dto.RecipeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PopularRecipeService {
    
    private final RecipePopularityRepository recipePopularityRepository;
    private final RecipePopularityHistoryRepository recipePopularityHistoryRepository;
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

        // 이전 순위 조회 (24시간 전)
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime yesterdayStart = yesterday.minusHours(1);
        LocalDateTime yesterdayEnd = yesterday.plusHours(1);
        
        List<RecipePopularityHistory> previousHistories = 
                recipePopularityHistoryRepository.findByRecordedAtBetween(yesterdayStart, yesterdayEnd);
        
        // 레시피 ID -> 이전 순위 매핑
        Map<Long, Integer> previousRanks = previousHistories.stream()
                .collect(Collectors.toMap(
                        RecipePopularityHistory::getRecipeId,
                        RecipePopularityHistory::getRank,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
        
        // DTO 변환
        List<PopularRecipeDto> result = new ArrayList<>();
        int rank = 1;
        
        for (RecipePopularity popularity : popularityList) {
            Recipe recipe = popularity.getRecipe();
            Long recipeId = recipe.getId();
            
            // 이전 순위 조회
            Integer previousRank = previousRanks.get(recipeId);
            
            // 트렌드 상태 계산
            String trendStatus;
            if (previousRank == null) {
                trendStatus = "NEW";
            } else if (rank < previousRank) {
                trendStatus = "UP";
            } else if (rank > previousRank) {
                trendStatus = "DOWN";
            } else {
                trendStatus = "SAME";
            }
            
            // RecipeDto 생성
            RecipeDto recipeDto = convertToRecipeDto(recipe);
            
            // PopularityStatsDto 생성
            PopularityStatsDto statsDto = PopularityStatsDto.from(popularity);
            
            // PopularRecipeDto 생성
            PopularRecipeDto popularRecipeDto = PopularRecipeDto.builder()
                    .rank(rank)
                    .previousRank(previousRank)
                    .trendStatus(trendStatus)
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

