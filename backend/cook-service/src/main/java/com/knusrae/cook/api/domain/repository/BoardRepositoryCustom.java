package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardRepositoryCustom {
    // 사용자별 게시글 조회
    List<Board> findAllByUserId(Long userId);

    // 제목으로 검색 (LIKE 검색)
    List<Board> findByTitleContaining(String keyword);

    // 내용으로 검색 (LIKE 검색)
    List<Board> findByContentContaining(String keyword);

    // 제목 또는 내용으로 검색
    List<Board> findByTitleOrContentContaining(String keyword);

    // 복합 조건 검색 (제목 + 내용)
    List<Board> findByTitleAndContent(String titleKeyword, String contentKeyword);

    // 인기 게시글 조회 (조회수 높은 순)
    List<Board> findPopularBoards(int limit);

    // 좋아요 수가 많은 게시글 조회
    List<Board> findMostLikedBoards(int limit);

    // 페이징 처리된 게시글 목록
    Page<Board> findBoardsWithPaging(String keyword, Long userId, Pageable pageable);

    // 사용자별 게시글 개수
    Long countByUserId(Long userId);

    // 검색 키워드에 따른 게시글 개수
    Long countByKeyword(String keyword);

    // 최근 N일 내 게시글 조회
    List<Board> findRecentBoards(int days, int limit);
}