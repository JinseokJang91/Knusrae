package com.knusrae.cook.api.theme.domain.service;

import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.repository.RecipeCommentRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeFavoriteRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeImageRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import com.knusrae.cook.api.theme.domain.entity.ThemeCollection;
import com.knusrae.cook.api.theme.domain.entity.ThemeCollectionRecipe;
import com.knusrae.cook.api.theme.domain.repository.ThemeCollectionRecipeRepository;
import com.knusrae.cook.api.theme.domain.repository.ThemeCollectionRepository;
import com.knusrae.cook.api.theme.dto.CreateThemeRequest;
import com.knusrae.cook.api.theme.dto.ThemeCollectionDetailDto;
import com.knusrae.cook.api.theme.dto.ThemeCollectionDto;
import com.knusrae.cook.api.theme.dto.ThemeRecipeItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ThemeCollectionService {

    private final ThemeCollectionRepository themeCollectionRepository;
    private final ThemeCollectionRecipeRepository themeCollectionRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final RecipeCommentRepository recipeCommentRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;
    private final MemberRepository memberRepository;

    /**
     * 현재 활성 테마 목록 조회
     */
    public List<ThemeCollectionDto> getActiveThemes() {
        log.debug("Fetching active themes");
        List<ThemeCollection> themes = themeCollectionRepository.findActiveThemesNow(LocalDate.now());

        return themes.stream()
                .map(theme -> {
                    long recipeCount = themeCollectionRecipeRepository.countByCollectionId(theme.getId());
                    return ThemeCollectionDto.from(theme, recipeCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * 테마별 레시피 목록 조회
     */
    public ThemeCollectionDetailDto getThemeRecipes(Long themeId, int limit, int offset) {
        log.debug("Fetching recipes for theme: {}, limit={}, offset={}", themeId, limit, offset);

        ThemeCollection theme = themeCollectionRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마를 찾을 수 없습니다. id=" + themeId));

        long recipeCount = themeCollectionRecipeRepository.countByCollectionId(themeId);
        ThemeCollectionDto themeDto = ThemeCollectionDto.from(theme, recipeCount);

        List<ThemeCollectionRecipe> themeRecipes =
                themeCollectionRecipeRepository.findByCollectionIdOrderBySortOrderAsc(themeId);

        List<ThemeCollectionRecipe> pagedRecipes = themeRecipes.stream()
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());

        List<ThemeRecipeItemDto> recipeDtos = pagedRecipes.stream()
                .map(tcr -> {
                    Recipe recipe = recipeRepository.findById(tcr.getRecipeId()).orElse(null);
                    if (recipe == null) return null;

                    String thumbnail = recipeImageRepository.findMainImageByRecipeId(recipe.getId())
                            .map(img -> img.getUrl())
                            .orElse(recipe.getThumbnail());

                    String memberNickname = memberRepository.findById(recipe.getMemberId())
                            .map(m -> m.getNickname())
                            .orElse(null);

                    long commentCount = recipeCommentRepository.countByRecipeId(recipe.getId());
                    long favoriteCount = recipeFavoriteRepository.countByRecipeId(recipe.getId());

                    return ThemeRecipeItemDto.builder()
                            .recipeId(recipe.getId())
                            .title(recipe.getTitle())
                            .thumbnail(thumbnail)
                            .memberId(recipe.getMemberId())
                            .memberNickname(memberNickname)
                            .hits(recipe.getHits())
                            .commentCount(commentCount)
                            .favoriteCount(favoriteCount)
                            .sortOrder(tcr.getSortOrder())
                            .build();
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        return ThemeCollectionDetailDto.builder()
                .theme(themeDto)
                .recipes(recipeDtos)
                .totalCount(recipeCount)
                .build();
    }

    /**
     * 테마 생성 (관리자)
     */
    @Transactional
    public ThemeCollectionDto createTheme(CreateThemeRequest request) {
        log.info("Creating new theme: {}", request.getName());

        ThemeCollection theme = ThemeCollection.builder()
                .name(request.getName())
                .description(request.getDescription())
                .thumbnailImage(request.getThumbnailImage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        ThemeCollection saved = themeCollectionRepository.save(theme);
        log.info("Theme created: id={}", saved.getId());

        return ThemeCollectionDto.from(saved, 0);
    }

    /**
     * 테마에 레시피 추가 (관리자)
     */
    @Transactional
    public ThemeCollectionRecipe addRecipeToTheme(Long themeId, Long recipeId, int sortOrder) {
        log.info("Adding recipe {} to theme {}", recipeId, themeId);

        if (!themeCollectionRepository.existsById(themeId)) {
            throw new IllegalArgumentException("테마를 찾을 수 없습니다. id=" + themeId);
        }
        if (!recipeRepository.existsById(recipeId)) {
            throw new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + recipeId);
        }
        if (themeCollectionRecipeRepository.existsByCollectionIdAndRecipeId(themeId, recipeId)) {
            throw new IllegalStateException("이미 추가된 레시피입니다.");
        }

        ThemeCollectionRecipe tcr = ThemeCollectionRecipe.builder()
                .collectionId(themeId)
                .recipeId(recipeId)
                .sortOrder(sortOrder)
                .build();

        return themeCollectionRecipeRepository.save(tcr);
    }

    /**
     * 테마에서 레시피 제거 (관리자)
     */
    @Transactional
    public void removeRecipeFromTheme(Long themeId, Long recipeId) {
        log.info("Removing recipe {} from theme {}", recipeId, themeId);

        ThemeCollectionRecipe tcr = themeCollectionRecipeRepository
                .findByCollectionIdAndRecipeId(themeId, recipeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 테마에 레시피가 존재하지 않습니다."));

        themeCollectionRecipeRepository.delete(tcr);
    }
}
