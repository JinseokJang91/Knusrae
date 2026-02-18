package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBook;
import com.knusrae.cook.api.recipe.domain.repository.RecipeBookRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeBookmarkRepository;
import com.knusrae.cook.api.recipe.dto.RecipeBookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecipeBookService {

    private final RecipeBookRepository recipeBookRepository;
    private final RecipeBookmarkRepository recipeBookmarkRepository;

    /**
     * 레시피북 생성
     */
    @Transactional
    public RecipeBookDto createRecipeBook(Long memberId, String name, String color) {
        log.info("Creating recipe book - memberId: {}, name: {}, color: {}", memberId, name, color);

        if (recipeBookRepository.existsByMemberIdAndName(memberId, name)) {
            throw new IllegalStateException("이미 존재하는 레시피북 이름입니다.");
        }

        long count = recipeBookRepository.countByMemberId(memberId);
        int sortOrder = (int) count;

        RecipeBook recipeBook = RecipeBook.builder()
                .memberId(memberId)
                .name(name)
                .color(color != null && !color.isEmpty() ? color : "blue")
                .sortOrder(sortOrder)
                .build();

        RecipeBook saved = recipeBookRepository.save(recipeBook);
        log.info("Recipe book created successfully - id: {}", saved.getId());

        return RecipeBookDto.from(saved, 0L);
    }

    /**
     * 회원의 레시피북 목록 조회 (북마크 개수 포함)
     */
    public List<RecipeBookDto> getRecipeBooksByMemberId(Long memberId) {
        log.info("Getting recipe books - memberId: {}", memberId);

        List<RecipeBook> recipeBooks = recipeBookRepository.findByMemberIdOrderBySortOrder(memberId);
        List<Long> recipeBookIds = recipeBooks.stream()
                .map(RecipeBook::getId)
                .collect(Collectors.toList());

        Map<Long, Long> bookmarkCountMap = new HashMap<>();
        if (!recipeBookIds.isEmpty()) {
            List<Object[]> counts = recipeBookmarkRepository.countByRecipeBookIds(recipeBookIds);
            for (Object[] count : counts) {
                Long recipeBookId = (Long) count[0];
                Long bookmarkCount = (Long) count[1];
                bookmarkCountMap.put(recipeBookId, bookmarkCount);
            }
        }

        return recipeBooks.stream()
                .map(rb -> RecipeBookDto.from(rb, bookmarkCountMap.getOrDefault(rb.getId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * 레시피북 정보 수정
     */
    @Transactional
    public RecipeBookDto updateRecipeBook(Long memberId, Long recipeBookId, String name, String color) {
        log.info("Updating recipe book - memberId: {}, recipeBookId: {}, name: {}, color: {}",
                memberId, recipeBookId, name, color);

        RecipeBook recipeBook = recipeBookRepository.findByMemberIdAndId(memberId, recipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        if (!recipeBook.getName().equals(name) &&
                recipeBookRepository.existsByMemberIdAndName(memberId, name)) {
            throw new IllegalStateException("이미 존재하는 레시피북 이름입니다.");
        }

        recipeBook.updateRecipeBook(name, color != null && !color.isEmpty() ? color : recipeBook.getColor());
        RecipeBook updated = recipeBookRepository.save(recipeBook);
        log.info("Recipe book updated successfully - id: {}", updated.getId());

        long bookmarkCount = recipeBookmarkRepository.countByRecipeBookId(recipeBookId);
        return RecipeBookDto.from(updated, bookmarkCount);
    }

    /**
     * 레시피북 삭제 (레시피북 내 북마크도 함께 삭제)
     */
    @Transactional
    public void deleteRecipeBook(Long memberId, Long recipeBookId) {
        log.info("Deleting recipe book - memberId: {}, recipeBookId: {}", memberId, recipeBookId);

        RecipeBook recipeBook = recipeBookRepository.findByMemberIdAndId(memberId, recipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        recipeBookmarkRepository.deleteByRecipeBookId(recipeBookId);
        recipeBookRepository.delete(recipeBook);
        log.info("Recipe book deleted successfully - id: {}", recipeBookId);
    }

    /**
     * 레시피북 순서 변경
     */
    @Transactional
    public List<RecipeBookDto> reorderRecipeBooks(Long memberId, List<Long> recipeBookIdOrder) {
        log.info("Reordering recipe books - memberId: {}, order: {}", memberId, recipeBookIdOrder);

        List<RecipeBook> recipeBooks = recipeBookRepository.findByMemberIdOrderBySortOrder(memberId);
        for (int i = 0; i < recipeBookIdOrder.size(); i++) {
            Long recipeBookId = recipeBookIdOrder.get(i);
            RecipeBook recipeBook = recipeBooks.stream()
                    .filter(rb -> rb.getId().equals(recipeBookId))
                    .findFirst()
                    .orElse(null);
            if (recipeBook != null) {
                recipeBook.updateSortOrder(i);
                recipeBookRepository.save(recipeBook);
            }
        }
        log.info("Recipe books reordered successfully");
        return getRecipeBooksByMemberId(memberId);
    }

    /**
     * 기본 레시피북 자동 생성 (첫 북마크 추가 시)
     */
    @Transactional
    public RecipeBookDto createDefaultRecipeBookIfNeeded(Long memberId) {
        log.info("Creating default recipe book if needed - memberId: {}", memberId);

        long count = recipeBookRepository.countByMemberId(memberId);
        if (count > 0) {
            List<RecipeBook> recipeBooks = recipeBookRepository.findByMemberIdOrderBySortOrder(memberId);
            if (!recipeBooks.isEmpty()) {
                long bookmarkCount = recipeBookmarkRepository.countByRecipeBookId(recipeBooks.get(0).getId());
                return RecipeBookDto.from(recipeBooks.get(0), bookmarkCount);
            }
        }
        return createRecipeBook(memberId, "기본 레시피북", "blue");
    }
}
