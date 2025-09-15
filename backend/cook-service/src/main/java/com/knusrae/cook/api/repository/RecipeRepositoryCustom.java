package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Recipe;

import java.util.List;

public interface RecipeRepositoryCustom {
    List<Recipe> findAllByCategory(String category);
}
