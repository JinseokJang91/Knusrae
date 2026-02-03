package com.knusrae.cook.api.recipe.web;

import com.knusrae.cook.api.recipe.domain.service.RecipeCommentService;
import com.knusrae.cook.api.recipe.dto.RecipeCommentDto;
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

        log.debug("Creating comment: recipeId={}, memberId={}, parentId={}", recipeId, memberId, parentId);

        RecipeCommentDto comment = recipeCommentService.createComment(recipeId, memberId, content, parentId);
        log.info("Comment created successfully: id={}, recipeId={}", comment.getId(), recipeId);

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
        log.debug("Creating comment with image: recipeId={}, memberId={}, parentId={}, hasImage={}",
                recipeId, memberId, parentId, image != null && !image.isEmpty());

        RecipeCommentDto comment = recipeCommentService.createCommentWithImage(recipeId, memberId, content, parentId, image);
        log.info("Comment with image created successfully: id={}, recipeId={}", comment.getId(), recipeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // READ - 특정 레시피의 댓글 목록 조회
    @GetMapping("/{recipeId}")
    public ResponseEntity<List<RecipeCommentDto>> getCommentsByRecipeId(@PathVariable Long recipeId) {
        log.debug("Fetching comments for recipe: {}", recipeId);
        List<RecipeCommentDto> comments = recipeCommentService.getCommentsByRecipeId(recipeId);
        log.info("Retrieved {} comments for recipe: {}", comments.size(), recipeId);
        return ResponseEntity.ok(comments);
    }
    
    // READ - 특정 레시피의 댓글 목록 조회 (Pagination 지원)
    @GetMapping("/{recipeId}/page")
    public ResponseEntity<Map<String, Object>> getCommentsByRecipeIdWithPagination(
            @PathVariable Long recipeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Fetching comments with pagination: recipeId={}, page={}, size={}", recipeId, page, size);
        Map<String, Object> response = recipeCommentService.getCommentsByRecipeIdWithPagination(recipeId, page, size);
        int commentCount = ((List<?>) response.get("comments")).size();
        log.info("Retrieved {} comments for recipe: {} (page: {}, size: {})", commentCount, recipeId, page, size);
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
        log.debug("Updating comment with image: id={}, memberId={}, removeImage={}, hasImage={}",
                commentId, memberId, removeImage, image != null && !image.isEmpty());

        RecipeCommentDto comment = recipeCommentService.updateCommentWithImage(commentId, memberId, content, image, removeImage);
        log.info("Comment with image updated successfully: id={}", comment.getId());

        return ResponseEntity.ok(comment);
    }

    // DELETE - 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long memberId
    ) {
        log.info("Deleting comment: id={}, memberId={}", commentId, memberId);
        recipeCommentService.deleteComment(commentId, memberId);
        log.info("Comment deleted successfully: id={}", commentId);
        return ResponseEntity.noContent().build();
    }

    // READ - 특정 레시피의 댓글 개수 조회
    @GetMapping("/{recipeId}/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long recipeId) {
        long count = recipeCommentService.getCommentCount(recipeId);
        return ResponseEntity.ok(count);
    }
}
