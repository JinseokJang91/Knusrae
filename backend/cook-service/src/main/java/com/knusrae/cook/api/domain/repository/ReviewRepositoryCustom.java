package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepositoryCustom {
    // 사용자별 리뷰 조회
    List<Review> findAllByUserId(Long userId);

    // 레시피별 리뷰 조회
    List<Review> findAllByRecipeId(Long recipeId);

    // 평점별 리뷰 조회
    List<Review> findAllByScore(Long score);

    // 평점 범위별 리뷰 조회
    List<Review> findByScoreRange(Long minScore, Long maxScore);

    // 제목으로 검색 (LIKE 검색)
    List<Review> findByTitleContaining(String keyword);

    // 내용으로 검색 (LIKE 검색)
    List<Review> findByContentContaining(String keyword);

    // 제목 또는 내용으로 검색
    List<Review> findByTitleOrContentContaining(String keyword);

    // 복합 조건 검색 (레시피 + 평점)
    List<Review> findByRecipeAndScore(Long recipeId, Long score);

    // 높은 평점 리뷰 조회 (평점 높은 순)
    List<Review> findHighRatedReviews(int limit);

    // 최근 리뷰 조회
    List<Review> findRecentReviews(int limit);

    // 페이징 처리된 리뷰 목록
    Page<Review> findReviewsWithPaging(String keyword, Long recipeId, Long userId, Long score, Pageable pageable);

    // 사용자별 리뷰 개수
    Long countByUserId(Long userId);

    // 레시피별 리뷰 개수
    Long countByRecipeId(Long recipeId);

    // 검색 키워드에 따른 리뷰 개수
    Long countByKeyword(String keyword);

    // 레시피별 평균 평점 계산
    Double getAverageScoreByRecipeId(Long recipeId);

    // 최근 N일 내 리뷰 조회
    List<Review> findRecentReviews(int days, int limit);
}