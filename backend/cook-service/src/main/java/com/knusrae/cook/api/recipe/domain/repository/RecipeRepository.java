package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.enums.Status;
import com.knusrae.cook.api.recipe.domain.enums.Visibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {
    List<Recipe> findAllByOrderByCreatedAtDesc();
    
    // 팔로잉 피드용: 특정 회원들의 공개된 승인된 레시피 조회
    List<Recipe> findByMemberIdInAndStatusAndVisibility(
            List<Long> memberIds, 
            Status status, 
            Visibility visibility,
            Pageable pageable
    );
}
