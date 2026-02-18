package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.RecipeBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeBookRepository extends JpaRepository<RecipeBook, Long> {

    /**
     * 회원의 레시피북 목록 조회 (정렬 순서대로)
     */
    List<RecipeBook> findByMemberIdOrderBySortOrder(Long memberId);

    /**
     * 회원의 특정 이름 레시피북 존재 여부 확인
     */
    boolean existsByMemberIdAndName(Long memberId, String name);

    /**
     * 회원의 특정 레시피북 조회
     */
    Optional<RecipeBook> findByMemberIdAndId(Long memberId, Long recipeBookId);

    /**
     * 회원의 레시피북 개수 조회
     */
    long countByMemberId(Long memberId);

    /**
     * 회원 ID로 레시피북 삭제
     */
    void deleteByMemberId(Long memberId);
}
