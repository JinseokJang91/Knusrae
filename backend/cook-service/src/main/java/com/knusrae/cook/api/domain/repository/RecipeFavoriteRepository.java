package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipeFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeFavoriteRepository extends JpaRepository<RecipeFavorite, Long> {
    
    // 특정 회원의 찜 목록 조회
    @Query("SELECT rf FROM RecipeFavorite rf WHERE rf.memberId = :memberId ORDER BY rf.createdAt DESC")
    List<RecipeFavorite> findByMemberId(@Param("memberId") Long memberId);
    
    // 특정 회원이 특정 레시피를 찜했는지 확인
    Optional<RecipeFavorite> findByMemberIdAndRecipeId(Long memberId, Long recipeId);
    
    // 특정 회원의 특정 레시피 찜 삭제
    void deleteByMemberIdAndRecipeId(Long memberId, Long recipeId);
    
    // 특정 회원이 특정 레시피를 찜했는지 여부 확인
    boolean existsByMemberIdAndRecipeId(Long memberId, Long recipeId);
    
    // 특정 레시피의 찜 개수 조회
    long countByRecipeId(Long recipeId);
    
    // 특정 레시피의 모든 찜 삭제 (레시피 삭제 시 사용)
    void deleteByRecipeId(Long recipeId);
    
    // 특정 레시피의 특정 기간 이후 찜 개수 조회 (인기도 계산용)
    @Query("SELECT COUNT(rf) FROM RecipeFavorite rf WHERE rf.recipeId = :recipeId AND rf.createdAt >= :since")
    long countByRecipeIdAndCreatedAtAfter(@Param("recipeId") Long recipeId, @Param("since") java.time.LocalDateTime since);
}

