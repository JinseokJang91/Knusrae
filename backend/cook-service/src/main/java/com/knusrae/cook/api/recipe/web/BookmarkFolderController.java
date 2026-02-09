package com.knusrae.cook.api.recipe.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.recipe.domain.service.BookmarkFolderService;
import com.knusrae.cook.api.recipe.dto.BookmarkFolderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe/bookmarks/folders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BookmarkFolderController {
    
    private final BookmarkFolderService bookmarkFolderService;
    
    /**
     * 회원의 폴더 목록 조회
     * GET /api/recipe/bookmarks/folders
     */
    @GetMapping
    public ResponseEntity<List<BookmarkFolderDto>> getFolders(Authentication authentication) {
        log.info("GET /api/recipe/bookmarks/folders - 폴더 목록 조회");
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<BookmarkFolderDto> folders = bookmarkFolderService.getFoldersByMemberId(memberId);
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            log.error("폴더 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 폴더 생성
     * POST /api/recipe/bookmarks/folders
     */
    @PostMapping
    public ResponseEntity<BookmarkFolderDto> createFolder(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String name = request.get("name");
        String color = request.get("color");
        
        log.info("POST /api/recipe/bookmarks/folders - 폴더 생성: name={}, color={}", name, color);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            BookmarkFolderDto folder = bookmarkFolderService.createFolder(memberId, name, color);
            return ResponseEntity.status(HttpStatus.CREATED).body(folder);
        } catch (IllegalStateException e) {
            log.warn("폴더 생성 실패 - 중복된 이름: {}", name);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("폴더 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 폴더 수정
     * PUT /api/recipe/bookmarks/folders/{folderId}
     */
    @PutMapping("/{folderId}")
    public ResponseEntity<BookmarkFolderDto> updateFolder(
            @PathVariable Long folderId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String name = request.get("name");
        String color = request.get("color");
        
        log.info("PUT /api/recipe/bookmarks/folders/{} - 폴더 수정: name={}, color={}", folderId, name, color);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            BookmarkFolderDto folder = bookmarkFolderService.updateFolder(memberId, folderId, name, color);
            return ResponseEntity.ok(folder);
        } catch (IllegalArgumentException e) {
            log.warn("폴더 수정 실패 - 존재하지 않는 폴더: {}", folderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("폴더 수정 실패 - 중복된 이름: {}", name);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("폴더 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 폴더 삭제
     * DELETE /api/recipe/bookmarks/folders/{folderId}
     */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable Long folderId,
            Authentication authentication) {
        log.info("DELETE /api/recipe/bookmarks/folders/{} - 폴더 삭제", folderId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            bookmarkFolderService.deleteFolder(memberId, folderId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("폴더 삭제 실패 - 존재하지 않는 폴더: {}", folderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("폴더 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 폴더 순서 변경
     * PUT /api/recipe/bookmarks/folders/reorder
     */
    @PutMapping("/reorder")
    public ResponseEntity<List<BookmarkFolderDto>> reorderFolders(
            @RequestBody Map<String, List<Long>> request,
            Authentication authentication) {
        List<Long> folderIdOrder = request.get("folderIdOrder");
        
        log.info("PUT /api/recipe/bookmarks/folders/reorder - 폴더 순서 변경: {}", folderIdOrder);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<BookmarkFolderDto> folders = bookmarkFolderService.reorderFolders(memberId, folderIdOrder);
            return ResponseEntity.ok(folders);
        } catch (Exception e) {
            log.error("폴더 순서 변경 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 기본 폴더 자동 생성
     * POST /api/recipe/bookmarks/folders/default
     */
    @PostMapping("/default")
    public ResponseEntity<BookmarkFolderDto> createDefaultFolder(Authentication authentication) {
        log.info("POST /api/recipe/bookmarks/folders/default - 기본 폴더 생성");
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            BookmarkFolderDto folder = bookmarkFolderService.createDefaultFolderIfNeeded(memberId);
            return ResponseEntity.ok(folder);
        } catch (Exception e) {
            log.error("기본 폴더 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
