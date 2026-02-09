package com.knusrae.cook.api.recipe.domain.service;

import com.knusrae.cook.api.recipe.domain.entity.BookmarkFolder;
import com.knusrae.cook.api.recipe.domain.repository.BookmarkFolderRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeBookmarkRepository;
import com.knusrae.cook.api.recipe.dto.BookmarkFolderDto;
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
public class BookmarkFolderService {
    
    private final BookmarkFolderRepository bookmarkFolderRepository;
    private final RecipeBookmarkRepository recipeBookmarkRepository;
    
    /**
     * 폴더 생성
     */
    @Transactional
    public BookmarkFolderDto createFolder(Long memberId, String name, String color) {
        log.info("Creating bookmark folder - memberId: {}, name: {}, color: {}", memberId, name, color);
        
        // 중복 이름 체크
        if (bookmarkFolderRepository.existsByMemberIdAndName(memberId, name)) {
            throw new IllegalStateException("이미 존재하는 폴더 이름입니다.");
        }
        
        // sortOrder 계산 (마지막 순서 + 1)
        long folderCount = bookmarkFolderRepository.countByMemberId(memberId);
        int sortOrder = (int) folderCount;
        
        BookmarkFolder folder = BookmarkFolder.builder()
                .memberId(memberId)
                .name(name)
                .color(color != null && !color.isEmpty() ? color : "blue")
                .sortOrder(sortOrder)
                .build();
        
        BookmarkFolder savedFolder = bookmarkFolderRepository.save(folder);
        log.info("Bookmark folder created successfully - id: {}", savedFolder.getId());
        
        return BookmarkFolderDto.from(savedFolder, 0L);
    }
    
    /**
     * 회원의 폴더 목록 조회 (북마크 개수 포함)
     */
    public List<BookmarkFolderDto> getFoldersByMemberId(Long memberId) {
        log.info("Getting bookmark folders - memberId: {}", memberId);
        
        List<BookmarkFolder> folders = bookmarkFolderRepository.findByMemberIdOrderBySortOrder(memberId);
        
        // 폴더별 북마크 개수 조회
        List<Long> folderIds = folders.stream()
                .map(BookmarkFolder::getId)
                .collect(Collectors.toList());
        
        Map<Long, Long> bookmarkCountMap = new HashMap<>();
        if (!folderIds.isEmpty()) {
            List<Object[]> counts = recipeBookmarkRepository.countByFolderIds(folderIds);
            for (Object[] count : counts) {
                Long folderId = (Long) count[0];
                Long bookmarkCount = (Long) count[1];
                bookmarkCountMap.put(folderId, bookmarkCount);
            }
        }
        
        return folders.stream()
                .map(folder -> BookmarkFolderDto.from(folder, bookmarkCountMap.getOrDefault(folder.getId(), 0L)))
                .collect(Collectors.toList());
    }
    
    /**
     * 폴더 정보 수정
     */
    @Transactional
    public BookmarkFolderDto updateFolder(Long memberId, Long folderId, String name, String color) {
        log.info("Updating bookmark folder - memberId: {}, folderId: {}, name: {}, color: {}", 
                memberId, folderId, name, color);
        
        BookmarkFolder folder = bookmarkFolderRepository.findByMemberIdAndId(memberId, folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        // 이름 변경 시 중복 체크 (자기 자신 제외)
        if (!folder.getName().equals(name) && 
            bookmarkFolderRepository.existsByMemberIdAndName(memberId, name)) {
            throw new IllegalStateException("이미 존재하는 폴더 이름입니다.");
        }
        
        folder.updateFolder(name, color != null && !color.isEmpty() ? color : folder.getColor());
        BookmarkFolder updatedFolder = bookmarkFolderRepository.save(folder);
        
        log.info("Bookmark folder updated successfully - id: {}", updatedFolder.getId());
        
        long bookmarkCount = recipeBookmarkRepository.countByFolderId(folderId);
        return BookmarkFolderDto.from(updatedFolder, bookmarkCount);
    }
    
    /**
     * 폴더 삭제 (폴더 내 북마크도 함께 삭제)
     */
    @Transactional
    public void deleteFolder(Long memberId, Long folderId) {
        log.info("Deleting bookmark folder - memberId: {}, folderId: {}", memberId, folderId);
        
        BookmarkFolder folder = bookmarkFolderRepository.findByMemberIdAndId(memberId, folderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));
        
        // 폴더 내 북마크 먼저 삭제
        recipeBookmarkRepository.deleteByFolderId(folderId);
        
        // 폴더 삭제
        bookmarkFolderRepository.delete(folder);
        
        log.info("Bookmark folder deleted successfully - id: {}", folderId);
    }
    
    /**
     * 폴더 순서 변경
     */
    @Transactional
    public List<BookmarkFolderDto> reorderFolders(Long memberId, List<Long> folderIdOrder) {
        log.info("Reordering bookmark folders - memberId: {}, order: {}", memberId, folderIdOrder);
        
        List<BookmarkFolder> folders = bookmarkFolderRepository.findByMemberIdOrderBySortOrder(memberId);
        
        // 순서 업데이트
        for (int i = 0; i < folderIdOrder.size(); i++) {
            Long folderId = folderIdOrder.get(i);
            BookmarkFolder folder = folders.stream()
                    .filter(f -> f.getId().equals(folderId))
                    .findFirst()
                    .orElse(null);
            
            if (folder != null) {
                folder.updateSortOrder(i);
                bookmarkFolderRepository.save(folder);
            }
        }
        
        log.info("Bookmark folders reordered successfully");
        
        return getFoldersByMemberId(memberId);
    }
    
    /**
     * 기본 폴더 자동 생성 (첫 북마크 추가 시)
     */
    @Transactional
    public BookmarkFolderDto createDefaultFolderIfNeeded(Long memberId) {
        log.info("Creating default folder if needed - memberId: {}", memberId);
        
        long folderCount = bookmarkFolderRepository.countByMemberId(memberId);
        if (folderCount > 0) {
            // 이미 폴더가 있으면 첫 번째 폴더 반환
            List<BookmarkFolder> folders = bookmarkFolderRepository.findByMemberIdOrderBySortOrder(memberId);
            if (!folders.isEmpty()) {
                long bookmarkCount = recipeBookmarkRepository.countByFolderId(folders.get(0).getId());
                return BookmarkFolderDto.from(folders.get(0), bookmarkCount);
            }
        }
        
        // 기본 폴더 생성
        return createFolder(memberId, "기본 폴더", "blue");
    }
}
