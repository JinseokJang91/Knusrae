package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Review;
import com.knusrae.cook.api.domain.QReview;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReview review = QReview.review;

    @Override
    public List<Review> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(review)
                .where(review.memberId.eq(userId))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findAllByRecipeId(Long recipeId) {
        return queryFactory
                .selectFrom(review)
                .where(review.recipe.id.eq(recipeId))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findAllByScore(Long score) {
        return queryFactory
                .selectFrom(review)
                .where(review.score.eq(score))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findByScoreRange(Long minScore, Long maxScore) {
        return queryFactory
                .selectFrom(review)
                .where(review.score.between(minScore, maxScore))
                .orderBy(review.score.desc(), review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findByTitleContaining(String keyword) {
        return queryFactory
                .selectFrom(review)
                .where(review.title.containsIgnoreCase(keyword))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findByContentContaining(String keyword) {
        return queryFactory
                .selectFrom(review)
                .where(review.content.containsIgnoreCase(keyword))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findByTitleOrContentContaining(String keyword) {
        return queryFactory
                .selectFrom(review)
                .where(review.title.containsIgnoreCase(keyword)
                        .or(review.content.containsIgnoreCase(keyword)))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findByRecipeAndScore(Long recipeId, Long score) {
        return queryFactory
                .selectFrom(review)
                .where(review.recipe.id.eq(recipeId)
                        .and(review.score.eq(score)))
                .orderBy(review.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Review> findHighRatedReviews(int limit) {
        return queryFactory
                .selectFrom(review)
                .orderBy(review.score.desc(), review.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Review> findRecentReviews(int limit) {
        return queryFactory
                .selectFrom(review)
                .orderBy(review.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Review> findReviewsWithPaging(String keyword, Long recipeId, Long userId, Long score, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색 조건 (제목 또는 내용에 포함)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(review.title.containsIgnoreCase(keyword)
                    .or(review.content.containsIgnoreCase(keyword)));
        }

        // 레시피 필터 조건
        if (recipeId != null) {
            builder.and(review.recipe.id.eq(recipeId));
        }

        // 사용자 필터 조건
        if (userId != null) {
            builder.and(review.memberId.eq(userId));
        }

        // 평점 필터 조건
        if (score != null) {
            builder.and(review.score.eq(score));
        }

        JPAQuery<Review> query = queryFactory
                .selectFrom(review)
                .where(builder)
                .orderBy(review.createdAt.desc());

        // 페이징 적용
        List<Review> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        long total = queryFactory
                .selectFrom(review)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Long countByUserId(Long userId) {
        return queryFactory
                .selectFrom(review)
                .where(review.memberId.eq(userId))
                .fetchCount();
    }

    @Override
    public Long countByRecipeId(Long recipeId) {
        return queryFactory
                .selectFrom(review)
                .where(review.recipe.id.eq(recipeId))
                .fetchCount();
    }

    @Override
    public Long countByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return queryFactory
                    .selectFrom(review)
                    .fetchCount();
        }

        return queryFactory
                .selectFrom(review)
                .where(review.title.containsIgnoreCase(keyword)
                        .or(review.content.containsIgnoreCase(keyword)))
                .fetchCount();
    }

    @Override
    public Double getAverageScoreByRecipeId(Long recipeId) {
        return queryFactory
                .select(review.score.avg())
                .from(review)
                .where(review.recipe.id.eq(recipeId))
                .fetchOne();
    }

    @Override
    public List<Review> findRecentReviews(int days, int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        return queryFactory
                .selectFrom(review)
                .where(review.createdAt.after(startDate))
                .orderBy(review.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}