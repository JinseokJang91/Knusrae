package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.domain.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, Long> {
    List<RecipeImage> findAllByRecipe(Recipe recipe);
}
