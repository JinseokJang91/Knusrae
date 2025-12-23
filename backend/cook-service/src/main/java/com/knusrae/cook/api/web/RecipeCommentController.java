package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.RecipeCommentService;
import com.knusrae.cook.api.dto.RecipeCommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipe/comments")
@RequiredArgsConstructor
@Slf4j
public class RecipeCommentController {

    private final RecipeCommentService recipeCommentService;

    // CREATE - 댓글 작성 (JSON)
    @PostMapping("/{recipeId}")
    public ResponseEntity<RecipeCommentDto> createComment(
            @PathVariable Long recipeId,
            @RequestBody Map<String, Object> request
    ) {
        Long memberId = Long.valueOf(request.get("memberId").toString());
        String content = request.get("content").toString();
        Long parentId = request.get("parentId") != null ? Long.valueOf(request.get("parentId").toString()) : null;

        log.info("[LOG][INPUT] Creating comment for recipe {}: memberId={}, content={}, parentId={}",
                recipeId, memberId, content, parentId);

        RecipeCommentDto comment = recipeCommentService.createComment(recipeId, memberId, content, parentId);
        log.info("[LOG][OUTPUT] Created comment: {}", comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // CREATE - 댓글 작성 (이미지 포함, multipart/form-data)
    @PostMapping(value = "/{recipeId}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeCommentDto> createCommentWithImage(
            @PathVariable Long recipeId,
            @RequestParam Long memberId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @RequestPart(required = false) MultipartFile image
    ) {
        log.info("[LOG][INPUT] Creating comment with image for recipe {}: memberId={}, content={}, parentId={}, hasImage={}",
                recipeId, memberId, content, parentId, image != null && !image.isEmpty());

        RecipeCommentDto comment = recipeCommentService.createCommentWithImage(recipeId, memberId, content, parentId, image);
        log.info("[LOG][OUTPUT] Created comment: {}", comment);

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // READ - 특정 레시피의 댓글 목록 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<List<RecipeCommentDto>> getCommentsByRecipeId(@PathVariable Long recipeId) {
        log.info("[LOG][INPUT] Fetching comments for recipe: {}", recipeId);
        List<RecipeCommentDto> comments = recipeCommentService.getCommentsByRecipeId(recipeId);
        log.info("[LOG][OUTPUT] Found {} comments", comments.size());
        return ResponseEntity.ok(comments);
    }
    
    // READ - 특정 레시피의 댓글 목록 조회 (Pagination 지원)
    @GetMapping("/{recipeId}/page")
    public ResponseEntity<Map<String, Object>> getCommentsByRecipeIdWithPagination(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("[LOG][INPUT] Fetching comments for recipe: {} (page: {}, size: {})", recipeId, page, size);
        Map<String, Object> response = recipeCommentService.getCommentsByRecipeIdWithPagination(recipeId, page, size);
        log.info("[LOG][OUTPUT] Found {} comments", ((List<?>) response.get("comments")).size());
        return ResponseEntity.ok(response);
    }

    // UPDATE - 댓글 수정 (JSON)
    @PutMapping("/{commentId}")
    public ResponseEntity<RecipeCommentDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request
    ) {
        Long memberId = Long.valueOf(request.get("memberId").toString());
        String content = request.get("content").toString();

        log.info("[LOG][INPUT] Updating comment {}: memberId={}, content={}", commentId, memberId, content);

        RecipeCommentDto comment = recipeCommentService.updateComment(commentId, memberId, content);
        log.info("[LOG][OUTPUT] Updated comment: {}", comment);

        return ResponseEntity.ok(comment);
    }

    // UPDATE - 댓글 수정 (이미지 포함, multipart/form-data)
    @PutMapping(value = "/{commentId}/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeCommentDto> updateCommentWithImage(
            @PathVariable Long commentId,
            @RequestParam Long memberId,
            @RequestParam String content,
            @RequestParam(defaultValue = "false") boolean removeImage,
            @RequestPart(required = false) MultipartFile image
    ) {
        log.info("[LOG][INPUT] Updating comment {} with image: memberId={}, content={}, removeImage={}, hasImage={}",
                commentId, memberId, content, removeImage, image != null && !image.isEmpty());

        RecipeCommentDto comment = recipeCommentService.updateCommentWithImage(commentId, memberId, content, image, removeImage);
        log.info("[LOG][OUTPUT] Updated comment: {}", comment);

        return ResponseEntity.ok(comment);
    }

    // DELETE - 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId
    ) {
        log.info("[LOG][INPUT] Deleting comment {}: memberId={}", commentId, memberId);
        recipeCommentService.deleteComment(commentId, memberId);
        log.info("[LOG][OUTPUT] Deleted comment: {}", commentId);
        return ResponseEntity.noContent().build();
    }

    // READ - 특정 레시피의 댓글 개수 조회
    @GetMapping("/{recipeId}/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long recipeId) {
        long count = recipeCommentService.getCommentCount(recipeId);
        return ResponseEntity.ok(count);
    }
}
