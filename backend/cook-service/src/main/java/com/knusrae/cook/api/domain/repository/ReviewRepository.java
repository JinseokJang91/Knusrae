package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    // 최신 리뷰 조회
    List<Review> findAllByOrderByCreatedAtDesc();

    // 사용자별 리뷰 조회
    List<Review> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 레시피별 리뷰 조회
    List<Review> findByRecipeIdOrderByCreatedAtDesc(Long recipeId);

    // 평점별 리뷰 조회
    List<Review> findByScoreOrderByCreatedAtDesc(Long score);

    // 사용자별 리뷰 개수
    Long countByMemberId(Long memberId);

    // 레시피별 리뷰 개수
    Long countByRecipeId(Long recipeId);
}