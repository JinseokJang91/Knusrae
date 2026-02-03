package com.knusrae.cook.api.ingredient.domain.repository;

import com.knusrae.cook.api.ingredient.domain.entity.Ingredient;
import com.knusrae.cook.api.ingredient.domain.entity.IngredientStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientStorageRepository extends JpaRepository<IngredientStorage, Long> {
    @Query("SELECT DISTINCT s.ingredient.group.id FROM IngredientStorage s")
    List<Long> findDistinctGroupIds();
    Optional<IngredientStorage> findByIngredientId(Long ingredientId);
    Optional<IngredientStorage> findByIngredient(Ingredient ingredient);
    boolean existsByIngredientId(Long ingredientId);
}
