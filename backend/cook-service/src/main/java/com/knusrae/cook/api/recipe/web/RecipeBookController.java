package com.knusrae.cook.api.recipe.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.cook.api.recipe.domain.service.RecipeBookService;
import com.knusrae.cook.api.recipe.dto.RecipeBookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe/bookmarks/recipe-books")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RecipeBookController {

    private final RecipeBookService recipeBookService;

    @GetMapping
    public ResponseEntity<List<RecipeBookDto>> getRecipeBooks(Authentication authentication) {
        log.info("GET /api/recipe/bookmarks/recipe-books - 레시피북 목록 조회");
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<RecipeBookDto> list = recipeBookService.getRecipeBooksByMemberId(memberId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("레시피북 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<RecipeBookDto> createRecipeBook(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String name = request.get("name");
        String color = request.get("color");
        log.info("POST /api/recipe/bookmarks/recipe-books - 레시피북 생성: name={}, color={}", name, color);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookDto dto = recipeBookService.createRecipeBook(memberId, name, color);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalStateException e) {
            log.warn("레시피북 생성 실패 - 중복된 이름: {}", name);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("레시피북 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{recipeBookId}")
    public ResponseEntity<RecipeBookDto> updateRecipeBook(
            @PathVariable Long recipeBookId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String name = request.get("name");
        String color = request.get("color");
        log.info("PUT /api/recipe/bookmarks/recipe-books/{} - 레시피북 수정", recipeBookId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookDto dto = recipeBookService.updateRecipeBook(memberId, recipeBookId, name, color);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            log.warn("레시피북 수정 실패 - 존재하지 않는 레시피북: {}", recipeBookId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            log.warn("레시피북 수정 실패 - 중복된 이름: {}", name);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("레시피북 수정 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{recipeBookId}")
    public ResponseEntity<Void> deleteRecipeBook(
            @PathVariable Long recipeBookId,
            Authentication authentication) {
        log.info("DELETE /api/recipe/bookmarks/recipe-books/{} - 레시피북 삭제", recipeBookId);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            recipeBookService.deleteRecipeBook(memberId, recipeBookId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("레시피북 삭제 실패 - 존재하지 않는 레시피북: {}", recipeBookId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("레시피북 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/reorder")
    public ResponseEntity<List<RecipeBookDto>> reorderRecipeBooks(
            @RequestBody Map<String, List<Long>> request,
            Authentication authentication) {
        List<Long> recipeBookIdOrder = request.get("recipeBookIdOrder");
        log.info("PUT /api/recipe/bookmarks/recipe-books/reorder - 레시피북 순서 변경: {}", recipeBookIdOrder);
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            List<RecipeBookDto> list = recipeBookService.reorderRecipeBooks(memberId, recipeBookIdOrder);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("레시피북 순서 변경 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/default")
    public ResponseEntity<RecipeBookDto> createDefaultRecipeBook(Authentication authentication) {
        log.info("POST /api/recipe/bookmarks/recipe-books/default - 기본 레시피북 생성");
        try {
            Long memberId = AuthenticationUtils.extractMemberId(authentication);
            RecipeBookDto dto = recipeBookService.createDefaultRecipeBookIfNeeded(memberId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("기본 레시피북 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
