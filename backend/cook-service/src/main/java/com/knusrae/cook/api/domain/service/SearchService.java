package com.knusrae.cook.api.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import com.knusrae.cook.api.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.domain.repository.RecipeRepository;
import com.knusrae.cook.api.dto.RecipeDto;
import com.knusrae.common.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 검색 기능을 제공하는 서비스
 * 여러 도메인(레시피, 게시판 등)의 검색 기능을 통합 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SearchService {
    private final RecipeRepository recipeRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final MemberRepository memberRepository;
    private final RecipeCommentRepository recipeCommentRepository;

    /**
     * 레시피 제목으로 검색 (공개된 레시피만)
     * 
     * @param keyword 검색어
     * @return 제목에 검색어가 포함된 공개 레시피 목록 (썸네일, 작성자 정보, 댓글 개수 포함)
     */
    public List<RecipeDto> searchRecipesByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Empty keyword provided for recipe search");
            return new ArrayList<>();
        }
        
        log.debug("Searching recipes with keyword: {}", keyword);
        List<Recipe> recipeList = recipeRepository.searchRecipesByTitle(keyword.trim());
        log.info("Found {} recipes matching keyword: {}", recipeList.size(), keyword);
        
        return setThumbnailsForRecipeList(recipeList);
    }

    /**
     * 레시피 목록에 썸네일, 작성자 정보, 댓글 개수 설정
     * 
     * @param recipeList 레시피 엔티티 목록
     * @return DTO 목록
     */
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

        // 2. 모든 Member ID 추출 및 한 번에 조회 (N+1 문제 해결)
        List<Long> memberIds = recipeList.stream()
                .map(Recipe::getMemberId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        // 3. 모든 댓글 개수를 한 번에 조회 (N+1 문제 해결)
        List<Object[]> commentCounts = recipeCommentRepository.countByRecipes(recipeList);
        Map<Long, Long> commentCountMap = commentCounts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        // 4. Recipe를 Map으로 변환 (효율적인 조회를 위해)
        Map<Long, Recipe> recipeMap = recipeList.stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        // 5. DTO에 데이터 설정
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
}

