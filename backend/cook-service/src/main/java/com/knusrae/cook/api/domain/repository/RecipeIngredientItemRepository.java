package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipeIngredientItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeIngredientItemRepository extends JpaRepository<RecipeIngredientItem, Long> {
}

