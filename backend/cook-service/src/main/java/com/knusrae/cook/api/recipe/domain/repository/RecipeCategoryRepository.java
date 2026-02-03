package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.entity.RecipeCategory;
import com.knusrae.cook.api.recipe.domain.entity.RecipeCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, RecipeCategoryId> {

    List<RecipeCategory> findAllByRecipe(Recipe recipe);

    void deleteAllByRecipe(Recipe recipe);
}
