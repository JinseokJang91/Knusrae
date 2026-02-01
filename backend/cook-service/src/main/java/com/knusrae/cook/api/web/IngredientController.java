package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.IngredientService;
import com.knusrae.cook.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@Slf4j
public class IngredientController {
    
    private final IngredientService ingredientService;
    
    /**
     * 재료 그룹 목록 조회
     */
    @GetMapping("/groups")
    public ResponseEntity<Map<String, Object>> getIngredientGroups() {
        log.debug("GET /api/ingredients/groups");
        
        List<IngredientGroupDto> groups = ingredientService.getAllGroups();
        
        Map<String, Object> response = new HashMap<>();
        response.put("groups", groups);
        
        log.info("Retrieved {} ingredient groups", groups.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 재료 목록 조회
     * @param type "storage" = 보관법 등록된 재료만, "preparation" = 손질법 등록된 재료만, 미지정 = 전체
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getIngredients(
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        log.debug("GET /api/ingredients - groupId={}, searchQuery={}, type={}, limit={}, offset={}",
                groupId, searchQuery, type, limit, offset);
        
        // limit 최대값 제한
        if (limit > 100) {
            limit = 100;
        }
        
        IngredientListResponseDto result = ingredientService.getIngredients(groupId, searchQuery, type, limit, offset);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ingredients", result.getIngredients());
        response.put("groups", result.getGroups());
        response.put("totalCount", result.getTotalCount());
        
        log.info("Retrieved {} ingredients", result.getIngredients().size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 재료 보관법 조회
     */
    @GetMapping("/{ingredientId}/storage")
    public ResponseEntity<Map<String, Object>> getIngredientStorage(@PathVariable Long ingredientId) {
        log.debug("GET /api/ingredients/{}/storage", ingredientId);
        
        IngredientStorageDto storage = ingredientService.getStorage(ingredientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", storage);
        
        log.info("Retrieved storage for ingredient: {}", ingredientId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 재료 손질법 조회
     */
    @GetMapping("/{ingredientId}/preparation")
    public ResponseEntity<Map<String, Object>> getIngredientPreparation(@PathVariable Long ingredientId) {
        log.debug("GET /api/ingredients/{}/preparation", ingredientId);
        
        IngredientPreparationDto preparation = ingredientService.getPreparation(ingredientId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", preparation);
        
        log.info("Retrieved preparation for ingredient: {}", ingredientId);
        return ResponseEntity.ok(response);
    }
}
