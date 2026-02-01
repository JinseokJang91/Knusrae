package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Ingredient;
import com.knusrae.cook.api.domain.entity.IngredientPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientPreparationRepository extends JpaRepository<IngredientPreparation, Long> {

    /** 손질법이 등록된 재료가 속한 그룹 ID 목록 (중복 제거) */
    @Query("SELECT DISTINCT p.ingredient.group.id FROM IngredientPreparation p")
    List<Long> findDistinctGroupIds();

    Optional<IngredientPreparation> findByIngredientId(Long ingredientId);
    
    Optional<IngredientPreparation> findByIngredient(Ingredient ingredient);
    
    boolean existsByIngredientId(Long ingredientId);
}
