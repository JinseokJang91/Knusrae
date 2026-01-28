package com.knusrae.cook.api.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.domain.service.IngredientRequestService;
import com.knusrae.cook.api.dto.IngredientRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredients/requests")
@RequiredArgsConstructor
@Slf4j
public class IngredientRequestController {
    
    private final IngredientRequestService requestService;
    
    /**
     * 재료 정보 요청 생성
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRequest(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        String ingredientName = request.get("ingredientName").toString();
        String requestType = request.get("requestType").toString();
        String message = request.getOrDefault("message", "").toString();
        
        Long memberId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                memberId = AuthenticationUtils.extractMemberId(authentication);
            } catch (Exception e) {
                log.debug("User not authenticated, creating anonymous request");
            }
        }
        
        log.debug("POST /api/ingredients/requests - ingredientName={}, requestType={}, memberId={}", 
                ingredientName, requestType, memberId);
        
        IngredientRequestDto created = requestService.createRequest(ingredientName, requestType, message, memberId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", created);
        
        log.info("Created ingredient request: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 사용자의 요청 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyRequests(Authentication authentication) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        
        log.debug("GET /api/ingredients/requests/my - memberId={}", memberId);
        
        List<IngredientRequestDto> requests = requestService.getUserRequests(memberId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("requests", requests);
        response.put("totalCount", requests.size());
        
        log.info("Retrieved {} requests for member: {}", requests.size(), memberId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 관리자용 요청 목록 조회
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        log.debug("GET /api/ingredients/requests/admin - page={}, size={}, status={}", page, size, status);
        
        Page<IngredientRequestDto> requests;
        if (status != null && !status.isBlank()) {
            requests = requestService.getRequestsByStatus(status, page, size);
        } else {
            requests = requestService.getAllRequests(page, size);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("requests", requests.getContent());
        response.put("totalCount", requests.getTotalElements());
        response.put("totalPages", requests.getTotalPages());
        response.put("currentPage", requests.getNumber());
        
        log.info("Retrieved {} requests (page {})", requests.getContent().size(), page);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 요청 상태 업데이트 (관리자)
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        String status = request.get("status").toString();
        
        log.debug("PUT /api/ingredients/requests/{}/status - status={}", id, status);
        
        IngredientRequestDto updated = requestService.updateRequestStatus(id, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", updated);
        
        log.info("Updated request status: {} -> {}", id, status);
        return ResponseEntity.ok(response);
    }
}
