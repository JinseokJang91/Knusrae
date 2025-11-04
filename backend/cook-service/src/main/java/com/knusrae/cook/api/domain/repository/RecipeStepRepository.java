package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.RecipeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeDetail, Long> {
}
