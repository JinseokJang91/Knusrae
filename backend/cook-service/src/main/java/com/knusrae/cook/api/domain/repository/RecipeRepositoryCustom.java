package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;

import java.util.List;

public interface RecipeRepositoryCustom {
    List<Recipe> findPublishedPublicRecipes();

    List<Recipe> findUserRecipes(Long userId);
}
