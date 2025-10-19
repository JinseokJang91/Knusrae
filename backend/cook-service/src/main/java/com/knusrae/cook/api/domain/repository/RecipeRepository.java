package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {
    List<Recipe> findAllByOrderByCreatedAtDesc();
}
