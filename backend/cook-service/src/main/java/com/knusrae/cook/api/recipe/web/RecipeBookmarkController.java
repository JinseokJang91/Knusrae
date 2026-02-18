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
     * 레시피북별 북마크 조회
     * GET /api/recipe/bookmarks/recipe-books/{recipeBookId}/bookmarks
     */
    @GetMapping("/recipe-books/{recipeBookId}/bookmarks")
    public ResponseEntity<List<RecipeBookmarkDto>> getBookmarksByRecipeBook(
            @PathVariable Long recipeBookId,
            Authentication authentication) {
        log.info("GET /api/recipe/bookmarks/recipe-books/{}/bookmarks - 북마크 조회", recipeBookId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<RecipeBookmarkDto> bookmarks = recipeBookmarkService.getBookmarksByRecipeBook(memberId, recipeBookId);
            return ResponseEntity.ok(bookmarks);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 조회 실패 - 존재하지 않는 레시피북: {}", recipeBookId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("북마크 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<RecipeBookmarkDto> addBookmark(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        Long recipeBookId = request.get("recipeBookId") != null ? ((Number) request.get("recipeBookId")).longValue() : null;
        Long recipeId = request.get("recipeId") != null ? ((Number) request.get("recipeId")).longValue() : null;
        String memo = request.get("memo") != null ? (String) request.get("memo") : null;
        log.info("POST /api/recipe/bookmarks - 북마크 추가: recipeBookId={}, recipeId={}", recipeBookId, recipeId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookmarkDto bookmark = recipeBookmarkService.addBookmark(memberId, recipeBookId, recipeId, memo);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookmark);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 추가 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("북마크 추가 실패 - 중복: recipeBookId={}, recipeId={}", recipeBookId, recipeId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("북마크 추가 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> removeBookmark(
            @RequestParam Long recipeBookId,
            @RequestParam Long recipeId,
            Authentication authentication) {
        log.info("DELETE /api/recipe/bookmarks - 북마크 제거: recipeBookId={}, recipeId={}", recipeBookId, recipeId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            recipeBookmarkService.removeBookmark(memberId, recipeBookId, recipeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("북마크 제거 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("북마크 제거 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    @PutMapping("/{bookmarkId}/move")
    public ResponseEntity<RecipeBookmarkDto> moveBookmark(
            @PathVariable Long bookmarkId,
            @RequestBody Map<String, Long> request,
            Authentication authentication) {
        Long targetRecipeBookId = request.get("targetRecipeBookId");
        log.info("PUT /api/recipe/bookmarks/{}/move - 북마크 이동: targetRecipeBookId={}", bookmarkId, targetRecipeBookId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookmarkDto bookmark = recipeBookmarkService.moveBookmark(memberId, bookmarkId, targetRecipeBookId);
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

    /**
     * 북마크 메모 수정
     * PUT /api/recipe/bookmarks/{bookmarkId}/memo
     */
    @PutMapping("/{bookmarkId}/memo")
    public ResponseEntity<RecipeBookmarkDto> updateBookmarkMemo(
            @PathVariable Long bookmarkId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String memo = request != null ? request.get("memo") : null;
        log.info("PUT /api/recipe/bookmarks/{}/memo - 메모 수정", bookmarkId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookmarkDto bookmark = recipeBookmarkService.updateMemo(memberId, bookmarkId, memo);
            return ResponseEntity.ok(bookmark);
        } catch (IllegalArgumentException e) {
            log.warn("북마크 메모 수정 실패 - {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("북마크 메모 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
