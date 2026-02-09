package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.recipe.domain.entity.BookmarkFolder;
import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeBookmark;
import com.knusrae.cook.api.recipe.domain.repository.BookmarkFolderRepository;
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
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    
    /**
     * 북마크 추가
     */
    @Transactional
    public RecipeBookmarkDto addBookmark(Long memberId, Long folderId, Long recipeId) {
        log.info("Adding bookmark - memberId: {}, folderId: {}, recipeId: {}", memberId, folderId, recipeId);
        
        // 폴더 존재 확인
        BookmarkFolder folder = bookmarkFolderRepository.findByMemberIdAndId(memberId, folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        // 레시피 존재 확인
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 레시피입니다."));
        
        // 중복 체크 (같은 폴더에 같은 레시피)
        if (recipeBookmarkRepository.existsByFolderIdAndRecipeId(folderId, recipeId)) {
            throw new IllegalStateException("이미 해당 폴더에 저장된 레시피입니다.");
        }
        
        RecipeBookmark bookmark = RecipeBookmark.builder()
                .folderId(folderId)
                .recipeId(recipeId)
                .memberId(memberId)
                .build();
        
        RecipeBookmark savedBookmark = recipeBookmarkRepository.save(bookmark);
        log.info("Bookmark added successfully - id: {}", savedBookmark.getId());
        
        return RecipeBookmarkDto.from(savedBookmark);
    }
    
    /**
     * 북마크 제거
     */
    @Transactional
    public void removeBookmark(Long memberId, Long folderId, Long recipeId) {
        log.info("Removing bookmark - memberId: {}, folderId: {}, recipeId: {}", memberId, folderId, recipeId);
        
        // 폴더 존재 확인
        bookmarkFolderRepository.findByMemberIdAndId(memberId, folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        // 북마크 존재 확인
        RecipeBookmark bookmark = recipeBookmarkRepository.findByFolderIdAndRecipeId(folderId, recipeId)
                .orElseThrow(() -> new IllegalArgumentException("북마크가 존재하지 않습니다."));
        
        recipeBookmarkRepository.delete(bookmark);
        log.info("Bookmark removed successfully - id: {}", bookmark.getId());
    }
    
    /**
     * 폴더별 북마크 조회
     */
    public List<RecipeBookmarkDto> getBookmarksByFolder(Long memberId, Long folderId) {
        log.info("Getting bookmarks by folder - memberId: {}, folderId: {}", memberId, folderId);
        
        // 폴더 존재 확인
        bookmarkFolderRepository.findByMemberIdAndId(memberId, folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        List<RecipeBookmark> bookmarks = recipeBookmarkRepository.findByFolderIdOrderByCreatedAtDesc(folderId);
        
        return bookmarks.stream()
                .map(bookmark -> {
                    Recipe recipe = recipeRepository.findById(bookmark.getRecipeId())
                            .orElse(null);
                    if (recipe == null) {
                        return null;
                    }
                    
                    Member member = memberRepository.findById(recipe.getMemberId())
                            .orElse(null);
                    String memberName = member != null ? member.getName() : "Unknown";
                    
                    RecipeSimpleDto recipeDto = RecipeSimpleDto.from(recipe, memberName);
                    return RecipeBookmarkDto.from(bookmark, recipeDto);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }
    
    /**
     * 레시피가 저장된 폴더 목록 조회
     */
    public Map<String, Object> checkBookmark(Long recipeId, Long memberId) {
        log.info("Checking bookmark - recipeId: {}, memberId: {}", recipeId, memberId);
        
        List<RecipeBookmark> bookmarks = recipeBookmarkRepository.findByRecipeIdAndMemberId(recipeId, memberId);
        List<Long> folderIds = bookmarks.stream()
                .map(RecipeBookmark::getFolderId)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("folders", folderIds);
        result.put("isBookmarked", !folderIds.isEmpty());
        
        return result;
    }
    
    /**
     * 북마크를 다른 폴더로 이동
     */
    @Transactional
    public RecipeBookmarkDto moveBookmark(Long memberId, Long bookmarkId, Long targetFolderId) {
        log.info("Moving bookmark - memberId: {}, bookmarkId: {}, targetFolderId: {}", 
                memberId, bookmarkId, targetFolderId);
        
        // 북마크 조회
        RecipeBookmark bookmark = recipeBookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 북마크입니다."));
        
        // 권한 확인
        if (!bookmark.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("북마크를 이동할 권한이 없습니다.");
        }
        
        // 대상 폴더 존재 확인
        bookmarkFolderRepository.findByMemberIdAndId(memberId, targetFolderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        // 대상 폴더에 이미 같은 레시피가 있는지 확인
        if (recipeBookmarkRepository.existsByFolderIdAndRecipeId(targetFolderId, bookmark.getRecipeId())) {
            throw new IllegalStateException("대상 폴더에 이미 같은 레시피가 저장되어 있습니다.");
        }
        
        // 기존 북마크 삭제 후 새로 생성 (이동)
        recipeBookmarkRepository.delete(bookmark);
        
        RecipeBookmark newBookmark = RecipeBookmark.builder()
                .folderId(targetFolderId)
                .recipeId(bookmark.getRecipeId())
                .memberId(memberId)
                .build();
        
        RecipeBookmark savedBookmark = recipeBookmarkRepository.save(newBookmark);
        log.info("Bookmark moved successfully - from folder: {}, to folder: {}", 
                bookmark.getFolderId(), targetFolderId);
        
        return RecipeBookmarkDto.from(savedBookmark);
    }
}
