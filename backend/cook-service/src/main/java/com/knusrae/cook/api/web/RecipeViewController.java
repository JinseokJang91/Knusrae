package com.knusrae.cook.api.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.domain.service.RecipeViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RecipeViewController {
    
    private final RecipeViewService recipeViewService;
    
    /**
     * 레시피 조회 기록 생성/갱신
     * 
     * @param recipeId 레시피 ID
     * @param authentication 인증 정보
     * @return 조회 기록 정보
     */
    @PostMapping("/recipes/{recipeId}/view")
    public ResponseEntity<Map<String, Object>> createRecipeView(
            @PathVariable Long recipeId,
            Authentication authentication
    ) {
        // 인증 확인
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        
        log.debug("Creating recipe view for member: {}, recipe: {}", memberId, recipeId);
        
        try {
            Map<String, Object> result = recipeViewService.createOrUpdateRecipeView(memberId, recipeId);
            
            // 성공 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            log.info("Recipe view created/updated successfully for member: {}, recipe: {}", memberId, recipeId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Recipe not found: {}", recipeId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            Map<String, String> error = new HashMap<>();
            error.put("code", "RECIPE_NOT_FOUND");
            error.put("message", e.getMessage());
            errorResponse.put("error", error);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            
        } catch (Exception e) {
            log.error("Error creating recipe view for member: {}, recipe: {}", memberId, recipeId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            Map<String, String> error = new HashMap<>();
            error.put("code", "INTERNAL_SERVER_ERROR");
            error.put("message", "오류가 발생했습니다.");
            errorResponse.put("error", error);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 최근 본 레시피 목록 조회
     * 
     * @param memberId 회원 ID
     * @param limit 조회할 개수 (기본값: 10)
     * @param offset 건너뛸 개수 (기본값: 0)
     * @param authentication 인증 정보
     * @return 최근 본 레시피 목록
     */
    @GetMapping("/members/{memberId}/recent-views")
    public ResponseEntity<Map<String, Object>> getRecentViews(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication
    ) {
        // 인증 확인 및 권한 체크 (본인 또는 관리자만)
        Long currentMemberId = AuthenticationUtils.extractMemberId(authentication);
        
        // 본인이 아닌 경우 권한 체크 (관리자 체크는 추후 구현)
        if (!currentMemberId.equals(memberId)) {
            log.warn("Unauthorized access attempt: member {} tried to access views of member {}", 
                    currentMemberId, memberId);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            Map<String, String> error = new HashMap<>();
            error.put("code", "FORBIDDEN");
            error.put("message", "본인의 조회 기록만 조회할 수 있습니다.");
            errorResponse.put("error", error);
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        log.debug("Fetching recent views for member: {}, limit: {}, offset: {}", 
                memberId, limit, offset);
        
        try {
            Map<String, Object> result = recipeViewService.getRecentViews(memberId, limit, offset);
            
            // 성공 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            
            log.info("Retrieved recent views for member: {}", memberId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching recent views for member: {}", memberId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            
            Map<String, String> error = new HashMap<>();
            error.put("code", "INTERNAL_SERVER_ERROR");
            error.put("message", "오류가 발생했습니다.");
            errorResponse.put("error", error);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 조회 기록 전체 삭제
     * 
     * @param memberId 회원 ID
     * @param authentication 인증 정보
     * @return 삭제 결과
     */
    @DeleteMapping("/members/{memberId}/recent-views")
    public ResponseEntity<Void> deleteAllRecentViews(
            @PathVariable Long memberId,
            Authentication authentication
    ) {
        // 인증 확인 및 권한 체크 (본인만)
        Long currentMemberId = AuthenticationUtils.extractMemberId(authentication);
        
        if (!currentMemberId.equals(memberId)) {
            log.warn("Unauthorized delete attempt: member {} tried to delete views of member {}", 
                    currentMemberId, memberId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.debug("Deleting all recent views for member: {}", memberId);
        
        try {
            recipeViewService.deleteAllViewsByMember(memberId);
            
            log.info("Deleted all recent views for member: {}", memberId);
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("Error deleting recent views for member: {}", memberId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
