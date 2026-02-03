package com.knusrae.cook.api.ingredient.domain.repository;

import com.knusrae.cook.api.ingredient.domain.entity.IngredientRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRequestRepository extends JpaRepository<IngredientRequest, Long> {
    List<IngredientRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    Page<IngredientRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
    Page<IngredientRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    Page<IngredientRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
