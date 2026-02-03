package com.knusrae.cook.api.ingredient.domain.repository;

import com.knusrae.cook.api.ingredient.domain.entity.Ingredient;
import com.knusrae.cook.api.ingredient.domain.entity.IngredientGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long>, JpaSpecificationExecutor<Ingredient> {
    List<Ingredient> findByGroupOrderBySortOrderAsc(IngredientGroup group);
    Page<Ingredient> findByGroupOrderBySortOrderAsc(IngredientGroup group, Pageable pageable);
    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Ingredient> findByNameContainingIgnoreCase(@Param("searchQuery") String searchQuery, Pageable pageable);
    @Query("SELECT i FROM Ingredient i WHERE i.group.id = :groupId AND LOWER(i.name) LIKE LOWER(CONCAT('%', :searchQuery, '%'))")
    Page<Ingredient> findByGroupIdAndNameContainingIgnoreCase(@Param("groupId") Long groupId, @Param("searchQuery") String searchQuery, Pageable pageable);
    Optional<Ingredient> findByName(String name);
}
