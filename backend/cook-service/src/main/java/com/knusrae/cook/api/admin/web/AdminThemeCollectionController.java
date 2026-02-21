package com.knusrae.cook.api.admin.web;

import com.knusrae.cook.api.theme.domain.entity.ThemeCollectionRecipe;
import com.knusrae.cook.api.theme.domain.service.ThemeCollectionService;
import com.knusrae.cook.api.theme.dto.CreateThemeRequest;
import com.knusrae.cook.api.theme.dto.ThemeCollectionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 테마 컬렉션 관리자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/themes")
@RequiredArgsConstructor
public class AdminThemeCollectionController {

    private final ThemeCollectionService themeCollectionService;

    /**
     * 테마 생성
     */
    @PostMapping
    public ResponseEntity<ThemeCollectionDto> createTheme(@Valid @RequestBody CreateThemeRequest request) {
        try {
            log.info("POST /api/admin/themes - name={}", request.getName());
            ThemeCollectionDto created = themeCollectionService.createTheme(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/admin/themes - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("POST /api/admin/themes - 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 테마에 레시피 추가
     */
    @PostMapping("/{themeId}/recipes")
    public ResponseEntity<ThemeCollectionRecipe> addRecipeToTheme(
            @PathVariable Long themeId,
            @RequestParam Long recipeId,
            @RequestParam(defaultValue = "0") int sortOrder
    ) {
        try {
            log.info("POST /api/admin/themes/{}/recipes - recipeId={}, sortOrder={}", themeId, recipeId, sortOrder);
            ThemeCollectionRecipe result = themeCollectionService.addRecipeToTheme(themeId, recipeId, sortOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("POST /api/admin/themes/{}/recipes - 찾을 수 없음: {}", themeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.error("POST /api/admin/themes/{}/recipes - 이미 추가된 레시피: {}", themeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("POST /api/admin/themes/{}/recipes - 오류 발생", themeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 테마에서 레시피 제거
     */
    @DeleteMapping("/{themeId}/recipes/{recipeId}")
    public ResponseEntity<Void> removeRecipeFromTheme(
            @PathVariable Long themeId,
            @PathVariable Long recipeId
    ) {
        try {
            log.info("DELETE /api/admin/themes/{}/recipes/{}", themeId, recipeId);
            themeCollectionService.removeRecipeFromTheme(themeId, recipeId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("DELETE /api/admin/themes/{}/recipes/{} - 찾을 수 없음: {}", themeId, recipeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("DELETE /api/admin/themes/{}/recipes/{} - 오류 발생", themeId, recipeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
