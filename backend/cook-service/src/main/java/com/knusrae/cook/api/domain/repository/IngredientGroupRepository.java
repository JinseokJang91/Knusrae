package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.IngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientGroupRepository extends JpaRepository<IngredientGroup, Long> {
    
    List<IngredientGroup> findAllByOrderBySortOrderAsc();
}
