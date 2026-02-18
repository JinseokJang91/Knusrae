package com.knusrae.cook.api.theme.domain.repository;

import com.knusrae.cook.api.theme.domain.entity.ThemeCollectionRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThemeCollectionRecipeRepository extends JpaRepository<ThemeCollectionRecipe, Long> {

    List<ThemeCollectionRecipe> findByCollectionIdOrderBySortOrderAsc(Long collectionId);

    boolean existsByCollectionIdAndRecipeId(Long collectionId, Long recipeId);

    Optional<ThemeCollectionRecipe> findByCollectionIdAndRecipeId(Long collectionId, Long recipeId);

    long countByCollectionId(Long collectionId);

    void deleteByCollectionId(Long collectionId);

    void deleteByCollectionIdAndRecipeId(Long collectionId, Long recipeId);
}
