package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Board;
import com.knusrae.cook.api.domain.QBoard;
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
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QBoard board = QBoard.board;

    @Override
    public List<Board> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(board)
                .where(board.memberId.eq(userId))
                .orderBy(board.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Board> findByTitleContaining(String keyword) {
        return queryFactory
                .selectFrom(board)
                .where(board.title.containsIgnoreCase(keyword))
                .orderBy(board.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Board> findByContentContaining(String keyword) {
        return queryFactory
                .selectFrom(board)
                .where(board.content.containsIgnoreCase(keyword))
                .orderBy(board.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Board> findByTitleOrContentContaining(String keyword) {
        return queryFactory
                .selectFrom(board)
                .where(board.title.containsIgnoreCase(keyword)
                        .or(board.content.containsIgnoreCase(keyword)))
                .orderBy(board.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Board> findByTitleAndContent(String titleKeyword, String contentKeyword) {
        return queryFactory
                .selectFrom(board)
                .where(board.title.containsIgnoreCase(titleKeyword)
                        .and(board.content.containsIgnoreCase(contentKeyword)))
                .orderBy(board.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Board> findPopularBoards(int limit) {
        return queryFactory
                .selectFrom(board)
                .orderBy(board.viewCount.desc(), board.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Board> findMostLikedBoards(int limit) {
        return queryFactory
                .selectFrom(board)
                .orderBy(board.likeCount.desc(), board.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Board> findBoardsWithPaging(String keyword, Long userId, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 검색 조건 (제목 또는 내용에 포함)
        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(board.title.containsIgnoreCase(keyword)
                    .or(board.content.containsIgnoreCase(keyword)));
        }

        // 사용자 필터 조건
        if (userId != null) {
            builder.and(board.memberId.eq(userId));
        }

        JPAQuery<Board> query = queryFactory
                .selectFrom(board)
                .where(builder)
                .orderBy(board.createdAt.desc());

        // 페이징 적용
        List<Board> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        long total = queryFactory
                .selectFrom(board)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Long countByUserId(Long userId) {
        return queryFactory
                .selectFrom(board)
                .where(board.memberId.eq(userId))
                .fetchCount();
    }

    @Override
    public Long countByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return queryFactory
                    .selectFrom(board)
                    .fetchCount();
        }

        return queryFactory
                .selectFrom(board)
                .where(board.title.containsIgnoreCase(keyword)
                        .or(board.content.containsIgnoreCase(keyword)))
                .fetchCount();
    }

    @Override
    public List<Board> findRecentBoards(int days, int limit) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        return queryFactory
                .selectFrom(board)
                .where(board.createdAt.after(startDate))
                .orderBy(board.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}