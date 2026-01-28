package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Ingredient;
import com.knusrae.cook.api.domain.entity.IngredientPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientPreparationRepository extends JpaRepository<IngredientPreparation, Long> {
    
    Optional<IngredientPreparation> findByIngredientId(Long ingredientId);
    
    Optional<IngredientPreparation> findByIngredient(Ingredient ingredient);
    
    boolean existsByIngredientId(Long ingredientId);
}
