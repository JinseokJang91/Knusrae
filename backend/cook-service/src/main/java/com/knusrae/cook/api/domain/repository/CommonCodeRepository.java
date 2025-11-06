package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.CommonCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {

    @EntityGraph(attributePaths = "details")
    List<CommonCode> findAllByCodeGroupAndUseYnOrderBySortAsc(String codeGroup, String useYn);
}

