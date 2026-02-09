package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeBookmarkRepository extends JpaRepository<RecipeBookmark, Long> {
    
    /**
     * 폴더별 북마크 목록 조회 (최신순)
     */
    List<RecipeBookmark> findByFolderIdOrderByCreatedAtDesc(Long folderId);
    
    /**
     * 폴더 내 특정 레시피 북마크 존재 여부 확인
     */
    boolean existsByFolderIdAndRecipeId(Long folderId, Long recipeId);
    
    /**
     * 폴더 내 특정 레시피 북마크 조회
     */
    Optional<RecipeBookmark> findByFolderIdAndRecipeId(Long folderId, Long recipeId);
    
    /**
     * 폴더 내 특정 레시피 북마크 삭제
     */
    void deleteByFolderIdAndRecipeId(Long folderId, Long recipeId);
    
    /**
     * 폴더 내 북마크 개수 조회
     */
    long countByFolderId(Long folderId);
    
    /**
     * 특정 레시피의 북마크 목록 조회 (어떤 폴더에 저장되었는지)
     */
    List<RecipeBookmark> findByRecipeIdAndMemberId(Long recipeId, Long memberId);
    
    /**
     * 회원의 특정 레시피 북마크 목록 조회
     */
    List<RecipeBookmark> findByMemberId(Long memberId);
    
    /**
     * 폴더 삭제 시 관련 북마크 모두 삭제
     */
    void deleteByFolderId(Long folderId);
    
    /**
     * 폴더별 북마크 개수 조회 (여러 폴더)
     */
    @Query("SELECT rb.folderId, COUNT(rb) FROM RecipeBookmark rb WHERE rb.folderId IN :folderIds GROUP BY rb.folderId")
    List<Object[]> countByFolderIds(@Param("folderIds") List<Long> folderIds);
}
