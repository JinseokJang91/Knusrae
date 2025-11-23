package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.RecipeFavoriteService;
import com.knusrae.cook.api.dto.RecipeFavoriteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe/favorites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecipeFavoriteController {
    
    private final RecipeFavoriteService recipeFavoriteService;
    
    /**
     * 특정 회원의 찜 목록 조회
     * GET /api/recipe/favorites/{memberId}
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<List<RecipeFavoriteDto>> getFavorites(@PathVariable Long memberId) {
        log.info("GET /api/recipe/favorites/{} - 찜 목록 조회", memberId);
        
        try {
            List<RecipeFavoriteDto> favorites = recipeFavoriteService.getFavoritesByMemberId(memberId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            log.error("찜 목록 조회 실패 - memberId: {}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 찜 추가
     * POST /api/recipe/favorites
     */
    @PostMapping
    public ResponseEntity<RecipeFavoriteDto> addFavorite(
            @RequestParam Long memberId,
            @RequestParam Long recipeId) {
        log.info("POST /api/recipe/favorites - 찜 추가: memberId={}, recipeId={}", memberId, recipeId);
        
        try {
            RecipeFavoriteDto favorite = recipeFavoriteService.addFavorite(memberId, recipeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
        } catch (IllegalStateException e) {
            log.warn("찜 추가 실패 - 이미 찜한 레시피: memberId={}, recipeId={}", memberId, recipeId);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.warn("찜 추가 실패 - 잘못된 요청: memberId={}, recipeId={}, message={}", memberId, recipeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("찜 추가 실패: memberId={}, recipeId={}", memberId, recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 찜 삭제
     * DELETE /api/recipe/favorites
     */
    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @RequestParam Long memberId,
            @RequestParam Long recipeId) {
        log.info("DELETE /api/recipe/favorites - 찜 삭제: memberId={}, recipeId={}", memberId, recipeId);
        
        try {
            recipeFavoriteService.removeFavorite(memberId, recipeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("찜 삭제 실패 - 찜하지 않은 레시피: memberId={}, recipeId={}", memberId, recipeId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("찜 삭제 실패: memberId={}, recipeId={}", memberId, recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 찜 토글 (있으면 삭제, 없으면 추가)
     * PUT /api/recipe/favorites/toggle
     */
    @PutMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @RequestParam Long memberId,
            @RequestParam Long recipeId) {
        log.info("PUT /api/recipe/favorites/toggle - 찜 토글: memberId={}, recipeId={}", memberId, recipeId);
        
        try {
            RecipeFavoriteDto favorite = recipeFavoriteService.toggleFavorite(memberId, recipeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("isFavorite", favorite != null);
            if (favorite != null) {
                response.put("favorite", favorite);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("찜 토글 실패: memberId={}, recipeId={}", memberId, recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 찜 여부 확인
     * GET /api/recipe/favorites/check
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(
            @RequestParam Long memberId,
            @RequestParam Long recipeId) {
        log.info("GET /api/recipe/favorites/check - 찜 여부 확인: memberId={}, recipeId={}", memberId, recipeId);
        
        try {
            boolean isFavorite = recipeFavoriteService.isFavorite(memberId, recipeId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isFavorite", isFavorite);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("찜 여부 확인 실패: memberId={}, recipeId={}", memberId, recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 레시피의 찜 개수 조회
     * GET /api/recipe/favorites/count/{recipeId}
     */
    @GetMapping("/count/{recipeId}")
    public ResponseEntity<Map<String, Long>> getFavoriteCount(@PathVariable Long recipeId) {
        log.info("GET /api/recipe/favorites/count/{} - 찜 개수 조회", recipeId);
        
        try {
            long count = recipeFavoriteService.getFavoriteCount(recipeId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("찜 개수 조회 실패: recipeId={}", recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

