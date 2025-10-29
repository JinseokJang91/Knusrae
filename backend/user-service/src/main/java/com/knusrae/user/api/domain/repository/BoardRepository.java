package com.knusrae.user.api.domain.repository;

import com.knusrae.user.api.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {
    // 최신 게시글 조회
    List<Board> findAllByOrderByCreatedAtDesc();

    // 사용자별 게시글 조회
    List<Board> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 제목으로 검색
    List<Board> findByTitleContainingOrderByCreatedAtDesc(String title);

    // 사용자별 게시글 개수
    Long countByMemberId(Long memberId);
}