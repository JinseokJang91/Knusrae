package com.knusrae.cook.api.recipe.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.recipe.domain.service.RecipeBookmarkService;
import com.knusrae.cook.api.recipe.dto.RecipeBookmarkDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe/bookmarks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecipeBookmarkController {
    
    private final RecipeBookmarkService recipeBookmarkService;
    
    /**
     * 폴더별 북마크 조회
     * GET /api/recipe/bookmarks/folder/{folderId}
     */
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<RecipeBookmarkDto>> getBookmarksByFolder(
            @PathVariable Long folderId,
            Authentication authentication) {
        log.info("GET /api/recipe/bookmarks/folder/{} - 북마크 조회", folderId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<RecipeBookmarkDto> bookmarks = recipeBookmarkService.getBookmarksByFolder(memberId, folderId);
            return ResponseEntity.ok(bookmarks);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 조회 실패 - 존재하지 않는 폴더: {}", folderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("북마크 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 북마크 추가
     * POST /api/recipe/bookmarks
     */
    @PostMapping
    public ResponseEntity<RecipeBookmarkDto> addBookmark(
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        Long folderId = request.get("folderId");
        Long recipeId = request.get("recipeId");
        
        log.info("POST /api/recipe/bookmarks - 북마크 추가: folderId={}, recipeId={}", folderId, recipeId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookmarkDto bookmark = recipeBookmarkService.addBookmark(memberId, folderId, recipeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookmark);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 추가 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("북마크 추가 실패 - 중복: folderId={}, recipeId={}", folderId, recipeId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("북마크 추가 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 북마크 제거
     * DELETE /api/recipe/bookmarks
     */
    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @RequestParam Long folderId,
            @RequestParam Long recipeId,
            Authentication authentication) {
        log.info("DELETE /api/recipe/bookmarks - 북마크 제거: folderId={}, recipeId={}", folderId, recipeId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            recipeBookmarkService.removeBookmark(memberId, folderId, recipeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("북마크 제거 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("북마크 제거 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 레시피 북마크 상태 확인
     * GET /api/recipe/bookmarks/check/{recipeId}
     */
    @GetMapping("/check/{recipeId}")
    public ResponseEntity<Map<String, Object>> checkBookmark(
            @PathVariable Long recipeId,
            Authentication authentication) {
        log.info("GET /api/recipe/bookmarks/check/{} - 북마크 상태 확인", recipeId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            Map<String, Object> result = recipeBookmarkService.checkBookmark(recipeId, memberId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("북마크 상태 확인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 북마크 이동
     * PUT /api/recipe/bookmarks/{bookmarkId}/move
     */
    @PutMapping("/{bookmarkId}/move")
    public ResponseEntity<RecipeBookmarkDto> moveBookmark(
            @PathVariable Long bookmarkId,
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        Long targetFolderId = request.get("targetFolderId");
        
        log.info("PUT /api/recipe/bookmarks/{}/move - 북마크 이동: targetFolderId={}", bookmarkId, targetFolderId);
        
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookmarkDto bookmark = recipeBookmarkService.moveBookmark(memberId, bookmarkId, targetFolderId);
            return ResponseEntity.ok(bookmark);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 이동 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("북마크 이동 실패 - 중복: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("북마크 이동 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
