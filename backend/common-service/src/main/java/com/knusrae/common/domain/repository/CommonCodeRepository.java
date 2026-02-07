package com.knusrae.common.domain.repository;

import com.knusrae.common.domain.entity.CommonCode;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, String> {

    @EntityGraph(attributePaths = "details")
    List<CommonCode> findAllByCodeGroupAndUseYn(String codeGroup, String useYn);

    @EntityGraph(attributePaths = "details")
    List<CommonCode> findAllByCodeIdAndUseYn(String codeId, String useYn);

    /** 관리자용: 전체 목록 조회 (코드그룹·코드ID 순) */
    @EntityGraph(attributePaths = "details")
    List<CommonCode> findAllByOrderByCodeGroupAscCodeIdAsc();
}

