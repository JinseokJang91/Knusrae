package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeIngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientGroupRepository extends JpaRepository<RecipeIngredientGroup, Long> {
    List<RecipeIngredientGroup> findAllByRecipe(Recipe recipe);
    void deleteAllByRecipe(Recipe recipe);
}
