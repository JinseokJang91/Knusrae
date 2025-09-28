package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.ReviewDto;
import com.knusrae.cook.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성
     */
    @PostMapping
    public ResponseEntity<ReviewDto.Response> createReview(@Valid @RequestBody ReviewDto.CreateRequest request) {
        log.info("POST /api/reviews - Creating review for recipe: {}", request.getRecipeId());

        ReviewDto.Response response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 리뷰 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReviewDto.Response> getReview(@PathVariable Long id) {
        log.info("GET /api/reviews/{} - Getting review", id);

        ReviewDto.Response response = reviewService.getReview(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto.Response> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDto.UpdateRequest request) {
        log.info("PUT /api/reviews/{} - Updating review", id);

        ReviewDto.Response response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("DELETE /api/reviews/{} - Deleting review", id);

        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 리뷰 조회 (최신순)
     */
    @GetMapping
    public ResponseEntity<List<ReviewDto.ListResponse>> getAllReviews() {
        log.info("GET /api/reviews - Getting all reviews");

        List<ReviewDto.ListResponse> response = reviewService.getAllReviews();
        return ResponseEntity.ok(response);
    }

    /**
     * 페이징 처리된 리뷰 목록 조회
     */
    @GetMapping("/paging")
    public ResponseEntity<Page<ReviewDto.ListResponse>> getReviewsWithPaging(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long recipeId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long score,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/reviews/paging - Getting reviews with paging");

        Page<ReviewDto.ListResponse> response = reviewService.getReviewsWithPaging(keyword, recipeId, userId, score, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 리뷰 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDto.ListResponse>> getReviewsByUserId(@PathVariable Long userId) {
        log.info("GET /api/reviews/user/{} - Getting reviews by user", userId);

        List<ReviewDto.ListResponse> response = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 레시피별 리뷰 조회
     */
    @GetMapping("/recipe/{recipeId}")
    public ResponseEntity<List<ReviewDto.ListResponse>> getReviewsByRecipeId(@PathVariable Long recipeId) {
        log.info("GET /api/reviews/recipe/{} - Getting reviews by recipe", recipeId);

        List<ReviewDto.ListResponse> response = reviewService.getReviewsByRecipeId(recipeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 평점별 리뷰 조회
     */
    @GetMapping("/score/{score}")
    public ResponseEntity<List<ReviewDto.ListResponse>> getReviewsByScore(@PathVariable Long score) {
        log.info("GET /api/reviews/score/{} - Getting reviews by score", score);

        List<ReviewDto.ListResponse> response = reviewService.getReviewsByScore(score);
        return ResponseEntity.ok(response);
    }

    /**
     * 높은 평점 리뷰 조회
     */
    @GetMapping("/high-rated")
    public ResponseEntity<List<ReviewDto.ListResponse>> getHighRatedReviews(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/reviews/high-rated - Getting high rated reviews with limit: {}", limit);

        List<ReviewDto.ListResponse> response = reviewService.getHighRatedReviews(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 리뷰 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<List<ReviewDto.ListResponse>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/reviews/recent - Getting recent reviews with limit: {}", limit);

        List<ReviewDto.ListResponse> response = reviewService.getRecentReviews(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 N일 내 리뷰 조회
     */
    @GetMapping("/recent-days")
    public ResponseEntity<List<ReviewDto.ListResponse>> getRecentReviewsByDays(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/reviews/recent-days - Getting recent reviews - days: {}, limit: {}", days, limit);

        List<ReviewDto.ListResponse> response = reviewService.getRecentReviews(days, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 제목으로 리뷰 검색
     */
    @GetMapping("/search/title")
    public ResponseEntity<List<ReviewDto.ListResponse>> searchReviewsByTitle(
            @RequestParam String keyword) {
        log.info("GET /api/reviews/search/title - Searching reviews by title keyword: {}", keyword);

        List<ReviewDto.ListResponse> response = reviewService.searchReviewsByTitle(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 내용으로 리뷰 검색
     */
    @GetMapping("/search/content")
    public ResponseEntity<List<ReviewDto.ListResponse>> searchReviewsByContent(
            @RequestParam String keyword) {
        log.info("GET /api/reviews/search/content - Searching reviews by content keyword: {}", keyword);

        List<ReviewDto.ListResponse> response = reviewService.searchReviewsByContent(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 제목 또는 내용으로 리뷰 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<ReviewDto.ListResponse>> searchReviewsByKeyword(
            @RequestParam String keyword) {
        log.info("GET /api/reviews/search - Searching reviews by keyword: {}", keyword);

        List<ReviewDto.ListResponse> response = reviewService.searchReviewsByKeyword(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 레시피별 평균 평점 조회
     */
    @GetMapping("/recipe/{recipeId}/average-score")
    public ResponseEntity<Double> getAverageScoreByRecipeId(@PathVariable Long recipeId) {
        log.info("GET /api/reviews/recipe/{}/average-score - Getting average score for recipe", recipeId);

        Double averageScore = reviewService.getAverageScoreByRecipeId(recipeId);
        return ResponseEntity.ok(averageScore);
    }
}