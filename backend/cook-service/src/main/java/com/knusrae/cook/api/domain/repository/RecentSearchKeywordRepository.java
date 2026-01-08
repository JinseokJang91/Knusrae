package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecentSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 최근 검색어 Repository
 */
@Repository
public interface RecentSearchKeywordRepository extends JpaRepository<RecentSearchKeyword, Long> {
    
    /**
     * 특정 회원의 최근 검색어 목록 조회 (최신순)
     * @param memberId 회원 ID
     * @return 최근 검색어 목록
     */
    @Query("SELECT rsk FROM RecentSearchKeyword rsk WHERE rsk.memberId = :memberId ORDER BY rsk.createdAt DESC")
    List<RecentSearchKeyword> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") Long memberId);
    
    /**
     * 특정 회원의 특정 검색어 조회
     * @param memberId 회원 ID
     * @param keyword 검색어
     * @return 최근 검색어 (존재하는 경우)
     */
    Optional<RecentSearchKeyword> findByMemberIdAndKeyword(Long memberId, String keyword);
    
    /**
     * 특정 회원의 최근 검색어 개수 조회
     * @param memberId 회원 ID
     * @return 검색어 개수
     */
    long countByMemberId(Long memberId);
    
    /**
     * 특정 회원의 최근 검색어 삭제
     * @param memberId 회원 ID
     * @param id 검색어 ID
     */
    void deleteByMemberIdAndId(Long memberId, Long id);
    
    /**
     * 특정 회원의 모든 최근 검색어 삭제
     * @param memberId 회원 ID
     */
    void deleteByMemberId(Long memberId);
}

