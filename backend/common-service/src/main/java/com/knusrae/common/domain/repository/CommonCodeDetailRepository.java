package com.knusrae.common.domain.repository;

import com.knusrae.common.domain.entity.CommonCodeDetail;
import com.knusrae.common.domain.entity.CommonCodeDetailId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, CommonCodeDetailId> {

    List<CommonCodeDetail> findAllByIdCodeIdInAndUseYn(List<String> codeIds, String useYn);
}

