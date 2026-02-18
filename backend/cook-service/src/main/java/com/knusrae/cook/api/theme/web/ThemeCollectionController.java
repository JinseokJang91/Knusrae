package com.knusrae.cook.api.theme.web;

import com.knusrae.cook.api.theme.domain.service.ThemeCollectionService;
import com.knusrae.cook.api.theme.dto.ThemeCollectionDetailDto;
import com.knusrae.cook.api.theme.dto.ThemeCollectionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 테마 컬렉션 공개 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class ThemeCollectionController {

    private final ThemeCollectionService themeCollectionService;

    /**
     * 활성 테마 목록 조회
     */
    @GetMapping("/active")
    public ResponseEntity<List<ThemeCollectionDto>> getActiveThemes() {
        try {
            log.info("GET /api/themes/active");
            List<ThemeCollectionDto> themes = themeCollectionService.getActiveThemes();
            return ResponseEntity.ok(themes);
        } catch (Exception e) {
            log.error("GET /api/themes/active - 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 테마별 레시피 목록 조회
     */
    @GetMapping("/{themeId}/recipes")
    public ResponseEntity<ThemeCollectionDetailDto> getThemeRecipes(
            @PathVariable Long themeId,
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        try {
            log.info("GET /api/themes/{}/recipes - limit={}, offset={}", themeId, limit, offset);
            ThemeCollectionDetailDto result = themeCollectionService.getThemeRecipes(themeId, limit, offset);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("GET /api/themes/{}/recipes - 테마를 찾을 수 없음: {}", themeId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("GET /api/themes/{}/recipes - 오류 발생", themeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
