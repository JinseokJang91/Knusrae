package com.knusrae.common.domain.repository;

import com.knusrae.common.domain.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    // 팔로우 관계 존재 여부
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // 팔로우 관계 조회
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
    
    // 특정 회원이 팔로우하는 사람 목록 (페이징)
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);
    
    // 특정 회원을 팔로우하는 사람 목록 (페이징)
    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(Long followingId, Pageable pageable);
    
    // 팔로워 수
    long countByFollowingId(Long followingId);
    
    // 팔로잉 수
    long countByFollowerId(Long followerId);
    
    // 특정 회원이 팔로우하는 크리에이터들의 ID 목록
    @Query("SELECT f.followingId FROM Follow f WHERE f.followerId = :memberId")
    List<Long> findFollowingIdsByFollowerId(Long memberId);
    
    // 팔로우 삭제
    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
