package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.entity.RecipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeImageRepository extends JpaRepository<RecipeImage, Long> {
    List<RecipeImage> findAllByRecipe(Recipe recipe);
    
    // 여러 레시피의 이미지를 한 번에 조회 (N+1 문제 해결)
    List<RecipeImage> findAllByRecipeIn(List<Recipe> recipes);
}
