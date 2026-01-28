package com.knusrae.cook.api.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.domain.service.AdminIngredientService;
import com.knusrae.cook.api.dto.IngredientPreparationDto;
import com.knusrae.cook.api.dto.IngredientStorageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ingredients")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminIngredientController {
    
    private final AdminIngredientService adminIngredientService;
    
    /**
     * 재료 보관법 등록
     */
    @PostMapping("/storage")
    public ResponseEntity<Map<String, Object>> createStorage(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Long ingredientId = Long.valueOf(request.get("ingredientId").toString());
        String content = request.get("content").toString();
        String summary = request.getOrDefault("summary", "").toString();
        Long adminId = AuthenticationUtils.extractMemberId(authentication);
        
        log.debug("POST /api/admin/ingredients/storage - ingredientId={}, adminId={}", ingredientId, adminId);
        
        IngredientStorageDto storage = adminIngredientService.createStorage(ingredientId, content, summary, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", storage);
        
        log.info("Created storage for ingredient: {}", ingredientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 재료 손질법 등록
     */
    @PostMapping("/preparation")
    public ResponseEntity<Map<String, Object>> createPreparation(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Long ingredientId = Long.valueOf(request.get("ingredientId").toString());
        String content = request.get("content").toString();
        String summary = request.getOrDefault("summary", "").toString();
        Long adminId = AuthenticationUtils.extractMemberId(authentication);
        
        log.debug("POST /api/admin/ingredients/preparation - ingredientId={}, adminId={}", ingredientId, adminId);
        
        IngredientPreparationDto preparation = adminIngredientService.createPreparation(ingredientId, content, summary, adminId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", preparation);
        
        log.info("Created preparation for ingredient: {}", ingredientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 재료 보관법 수정
     */
    @PutMapping("/storage/{id}")
    public ResponseEntity<Map<String, Object>> updateStorage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        String content = request.get("content").toString();
        String summary = request.getOrDefault("summary", "").toString();
        
        log.debug("PUT /api/admin/ingredients/storage/{}", id);
        
        IngredientStorageDto storage = adminIngredientService.updateStorage(id, content, summary);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", storage);
        
        log.info("Updated storage: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 재료 손질법 수정
     */
    @PutMapping("/preparation/{id}")
    public ResponseEntity<Map<String, Object>> updatePreparation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        
        String content = request.get("content").toString();
        String summary = request.getOrDefault("summary", "").toString();
        
        log.debug("PUT /api/admin/ingredients/preparation/{}", id);
        
        IngredientPreparationDto preparation = adminIngredientService.updatePreparation(id, content, summary);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", preparation);
        
        log.info("Updated preparation: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 본문용 이미지 업로드 (에디터 addImageBlobHook).
     * multipart/form-data, 파트명 "image".
     */
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(@RequestPart("image") MultipartFile image) {
        log.debug("POST /api/admin/ingredients/upload-image");
        String url = adminIngredientService.uploadContentImage(image);
        Map<String, String> body = new HashMap<>();
        body.put("url", url);
        log.info("Uploaded content image: {}", url);
        return ResponseEntity.ok(body);
    }

    /**
     * 재료 보관법 삭제
     */
    @DeleteMapping("/storage/{id}")
    public ResponseEntity<Void> deleteStorage(@PathVariable Long id) {
        log.debug("DELETE /api/admin/ingredients/storage/{}", id);
        
        adminIngredientService.deleteStorage(id);
        
        log.info("Deleted storage: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 재료 손질법 삭제
     */
    @DeleteMapping("/preparation/{id}")
    public ResponseEntity<Void> deletePreparation(@PathVariable Long id) {
        log.debug("DELETE /api/admin/ingredients/preparation/{}", id);
        
        adminIngredientService.deletePreparation(id);
        
        log.info("Deleted preparation: {}", id);
        return ResponseEntity.noContent().build();
    }
}
