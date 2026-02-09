package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.BookmarkFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkFolderRepository extends JpaRepository<BookmarkFolder, Long> {
    
    /**
     * 회원의 폴더 목록 조회 (정렬 순서대로)
     */
    List<BookmarkFolder> findByMemberIdOrderBySortOrder(Long memberId);
    
    /**
     * 회원의 특정 이름 폴더 존재 여부 확인
     */
    boolean existsByMemberIdAndName(Long memberId, String name);
    
    /**
     * 회원의 특정 폴더 조회
     */
    Optional<BookmarkFolder> findByMemberIdAndId(Long memberId, Long folderId);
    
    /**
     * 회원의 폴더 개수 조회
     */
    long countByMemberId(Long memberId);
    
    /**
     * 회원 ID로 폴더 삭제
     */
    void deleteByMemberId(Long memberId);
}
