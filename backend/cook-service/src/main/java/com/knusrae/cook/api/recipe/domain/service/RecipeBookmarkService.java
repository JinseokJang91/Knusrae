package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.exception.ResourceNotFoundException;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeBook;
import com.knusrae.cook.api.recipe.domain.entity.RecipeBookmark;
import com.knusrae.cook.api.recipe.domain.repository.RecipeBookRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeBookmarkRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import com.knusrae.cook.api.recipe.dto.RecipeBookmarkDto;
import com.knusrae.cook.api.recipe.dto.RecipeSimpleDto;
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
public class RecipeBookmarkService {

    private final RecipeBookmarkRepository recipeBookmarkRepository;
    private final RecipeBookRepository recipeBookRepository;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public RecipeBookmarkDto addBookmark(Long memberId, Long recipeBookId, Long recipeId, String memo) {
        log.info("Adding bookmark - memberId: {}, recipeBookId: {}, recipeId: {}", memberId, recipeBookId, recipeId);

        RecipeBook recipeBook = recipeBookRepository.findByMemberIdAndId(memberId, recipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));

        if (recipeBookmarkRepository.existsByRecipeBookIdAndRecipeId(recipeBookId, recipeId)) {
            throw new IllegalStateException("이미 해당 레시피북에 저장된 레시피입니다.");
        }

        RecipeBookmark bookmark = RecipeBookmark.builder()
                .recipeBookId(recipeBookId)
                .recipeId(recipeId)
                .memberId(memberId)
                .memo(memo != null && !memo.isBlank() ? memo.trim() : null)
                .build();

        RecipeBookmark savedBookmark = recipeBookmarkRepository.save(bookmark);
        log.info("Bookmark added successfully - id: {}", savedBookmark.getId());
        return RecipeBookmarkDto.from(savedBookmark);
    }

    @Transactional
    public void removeBookmark(Long memberId, Long recipeBookId, Long recipeId) {
        log.info("Removing bookmark - memberId: {}, recipeBookId: {}, recipeId: {}", memberId, recipeBookId, recipeId);

        recipeBookRepository.findByMemberIdAndId(memberId, recipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        RecipeBookmark bookmark = recipeBookmarkRepository.findByRecipeBookIdAndRecipeId(recipeBookId, recipeId)
                .orElseThrow(() -> new IllegalArgumentException("북마크가 존재하지 않습니다."));

        recipeBookmarkRepository.delete(bookmark);
        log.info("Bookmark removed successfully - id: {}", bookmark.getId());
    }

    public List<RecipeBookmarkDto> getBookmarksByRecipeBook(Long memberId, Long recipeBookId) {
        log.info("Getting bookmarks by recipe book - memberId: {}, recipeBookId: {}", memberId, recipeBookId);

        recipeBookRepository.findByMemberIdAndId(memberId, recipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        List<RecipeBookmark> bookmarks = recipeBookmarkRepository.findByRecipeBookIdOrderByCreatedAtDesc(recipeBookId);
        return bookmarks.stream()
                .map(bookmark -> {
                    Recipe recipe = recipeRepository.findById(bookmark.getRecipeId())
                            .orElseThrow(() -> new ResourceNotFoundException("레시피를 찾을 수 없습니다: " + bookmark.getRecipeId()));
                    Member member = memberRepository.findById(recipe.getMemberId())
                            .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + recipe.getMemberId()));
                    RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe, member.getName());
                    return RecipeBookmarkDto.from(bookmark, recipeDto);
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> checkBookmark(Long recipeId, Long memberId) {
        log.info("Checking bookmark - recipeId: {}, memberId: {}", recipeId, memberId);

        List<RecipeBookmark> bookmarks = recipeBookmarkRepository.findByRecipeIdAndMemberId(recipeId, memberId);
        List<Long> recipeBookIds = bookmarks.stream()
                .map(RecipeBookmark::getRecipeBookId)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("recipeBooks", recipeBookIds);
        result.put("isBookmarked", !recipeBookIds.isEmpty());
        return result;
    }

    @Transactional
    public RecipeBookmarkDto moveBookmark(Long memberId, Long bookmarkId, Long targetRecipeBookId) {
        log.info("Moving bookmark - memberId: {}, bookmarkId: {}, targetRecipeBookId: {}",
                memberId, bookmarkId, targetRecipeBookId);

        RecipeBookmark bookmark = recipeBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 북마크입니다."));

        if (!bookmark.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("북마크를 이동할 권한이 없습니다.");
        }

        recipeBookRepository.findByMemberIdAndId(memberId, targetRecipeBookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피북입니다."));

        if (recipeBookmarkRepository.existsByRecipeBookIdAndRecipeId(targetRecipeBookId, bookmark.getRecipeId())) {
            throw new IllegalStateException("대상 레시피북에 이미 같은 레시피가 저장되어 있습니다.");
        }

        recipeBookmarkRepository.delete(bookmark);
        RecipeBookmark newBookmark = RecipeBookmark.builder()
                .recipeBookId(targetRecipeBookId)
                .recipeId(bookmark.getRecipeId())
                .memberId(memberId)
                .memo(bookmark.getMemo())
                .build();
        RecipeBookmark savedBookmark = recipeBookmarkRepository.save(newBookmark);
        log.info("Bookmark moved successfully - to recipeBookId: {}", targetRecipeBookId);
        return RecipeBookmarkDto.from(savedBookmark);
    }

    @Transactional
    public RecipeBookmarkDto updateMemo(Long memberId, Long bookmarkId, String memo) {
        log.info("Updating bookmark memo - memberId: {}, bookmarkId: {}", memberId, bookmarkId);

        RecipeBookmark bookmark = recipeBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 북마크입니다."));

        if (!bookmark.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("메모를 수정할 권한이 없습니다.");
        }

        bookmark.updateMemo(memo);
        recipeBookmarkRepository.flush();
        Recipe recipe = recipeRepository.findById(bookmark.getRecipeId())
                .orElseThrow(() -> new ResourceNotFoundException("레시피를 찾을 수 없습니다: " + bookmark.getRecipeId()));
        Member member = memberRepository.findById(recipe.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다: " + recipe.getMemberId()));
        RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe, member.getName());
        return RecipeBookmarkDto.from(bookmark, recipeDto);
    }
}
