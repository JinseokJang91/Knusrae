package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Ingredient;
import com.knusrae.cook.api.domain.entity.IngredientStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IngredientStorageRepository extends JpaRepository<IngredientStorage, Long> {
    
    Optional<IngredientStorage> findByIngredientId(Long ingredientId);
    
    Optional<IngredientStorage> findByIngredient(Ingredient ingredient);
    
    boolean existsByIngredientId(Long ingredientId);
}
