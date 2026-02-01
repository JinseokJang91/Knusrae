package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.IngredientGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientGroupRepository extends JpaRepository<IngredientGroup, Long> {

    List<IngredientGroup> findAllByOrderBySortOrderAsc();

    /** 지정한 ID 목록에 해당하는 그룹을 sortOrder 순으로 조회 */
    List<IngredientGroup> findAllByIdInOrderBySortOrderAsc(List<Long> ids);
}
