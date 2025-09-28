package com.knusrae.cook.api.service;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.domain.Review;
import com.knusrae.cook.api.dto.ReviewDto;
import com.knusrae.cook.api.repository.RecipeRepository;
import com.knusrae.cook.api.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RecipeRepository recipeRepository;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewDto.Response createReview(ReviewDto.CreateRequest request) {
        log.info("Creating new review for recipe id: {}", request.getRecipeId());

        // 레시피 존재 확인
        Recipe recipe = recipeRepository.findById(request.getRecipeId())
                .orElseThrow(() -> new RuntimeException("레시피를 찾을 수 없습니다. ID: " + request.getRecipeId()));

        Review review = Review.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .memberId(request.getMemberId())
                .score(request.getScore())
                .recipe(recipe)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with id: {}", savedReview.getId());

        return ReviewDto.Response.from(savedReview);
    }

    /**
     * 리뷰 조회
     */
    public ReviewDto.Response getReview(Long id) {
        log.info("Getting review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다. ID: " + id));

        return ReviewDto.Response.from(review);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewDto.Response updateReview(Long id, ReviewDto.UpdateRequest request) {
        log.info("Updating review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다. ID: " + id));

        review.updateReview(request.getTitle(), request.getContent(), request.getImages(), request.getScore());

        return ReviewDto.Response.from(review);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long id) {
        log.info("Deleting review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다. ID: " + id));

        reviewRepository.delete(review);
        log.info("Review deleted successfully with id: {}", id);
    }

    /**
     * 모든 리뷰 조회 (최신순)
     */
    public List<ReviewDto.ListResponse> getAllReviews() {
        log.info("Getting all reviews");

        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자별 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getReviewsByUserId(Long userId) {
        log.info("Getting reviews by user id: {}", userId);

        return reviewRepository.findByMemberIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 레시피별 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getReviewsByRecipeId(Long recipeId) {
        log.info("Getting reviews by recipe id: {}", recipeId);

        return reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipeId)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 평점별 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getReviewsByScore(Long score) {
        log.info("Getting reviews by score: {}", score);

        return reviewRepository.findByScoreOrderByCreatedAtDesc(score)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페이징 처리된 리뷰 목록 조회
     */
    public Page<ReviewDto.ListResponse> getReviewsWithPaging(String keyword, Long recipeId, Long userId, Long score, Pageable pageable) {
        log.info("Getting reviews with paging - keyword: {}, recipeId: {}, userId: {}, score: {}, page: {}",
                keyword, recipeId, userId, score, pageable.getPageNumber());

        Page<Review> reviewPage = reviewRepository.findReviewsWithPaging(keyword, recipeId, userId, score, pageable);

        return reviewPage.map(ReviewDto.ListResponse::from);
    }

    /**
     * 높은 평점 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getHighRatedReviews(int limit) {
        log.info("Getting high rated reviews with limit: {}", limit);

        return reviewRepository.findHighRatedReviews(limit)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getRecentReviews(int limit) {
        log.info("Getting recent reviews with limit: {}", limit);

        return reviewRepository.findRecentReviews(limit)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 N일 내 리뷰 조회
     */
    public List<ReviewDto.ListResponse> getRecentReviews(int days, int limit) {
        log.info("Getting recent reviews - days: {}, limit: {}", days, limit);

        return reviewRepository.findRecentReviews(days, limit)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 레시피별 평균 평점 조회
     */
    public Double getAverageScoreByRecipeId(Long recipeId) {
        log.info("Getting average score for recipe id: {}", recipeId);

        return reviewRepository.getAverageScoreByRecipeId(recipeId);
    }

    /**
     * 제목으로 리뷰 검색
     */
    public List<ReviewDto.ListResponse> searchReviewsByTitle(String keyword) {
        log.info("Searching reviews by title keyword: {}", keyword);

        return reviewRepository.findByTitleContaining(keyword)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 내용으로 리뷰 검색
     */
    public List<ReviewDto.ListResponse> searchReviewsByContent(String keyword) {
        log.info("Searching reviews by content keyword: {}", keyword);

        return reviewRepository.findByContentContaining(keyword)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 제목 또는 내용으로 리뷰 검색
     */
    public List<ReviewDto.ListResponse> searchReviewsByKeyword(String keyword) {
        log.info("Searching reviews by keyword: {}", keyword);

        return reviewRepository.findByTitleOrContentContaining(keyword)
                .stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }
}